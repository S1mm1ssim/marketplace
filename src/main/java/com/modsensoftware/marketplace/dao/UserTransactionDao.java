package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Order;
import com.modsensoftware.marketplace.domain.UserTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserTransactionDao implements Dao<UserTransaction, Long> {

    private final SessionFactory sessionFactory;

    private static final String TRANSACTION_ENTITY_GRAPH = "graph.UserTransaction.orders.position";
    private static final String GRAPH_TYPE = "javax.persistence.loadgraph";

    private static final String TRANSACTION_USER_ID = "userId";
    private static final String TIMESTAMP_CREATED = "created";

    @Value("${default.page.size}")
    private int pageSize;

    @Override
    public UserTransaction get(Long id) {
        return null;
    }

    @Override
    public List<UserTransaction> getAll(int pageNumber, Map<String, String> filterProperties) {
        UUID userId = UUID.fromString(filterProperties.get(TRANSACTION_USER_ID));
        if (log.isDebugEnabled()) {
            log.debug("Fetching all transactions for page {} for user with id {}", pageNumber,
                    filterProperties.get(TRANSACTION_USER_ID));
        }
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(TRANSACTION_ENTITY_GRAPH);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UserTransaction> getAll = cb.createQuery(UserTransaction.class);
        Root<UserTransaction> root = getAll.from(UserTransaction.class);

        getAll.select(root).where(
                cb.equal(root.get(TRANSACTION_USER_ID), userId)
        ).orderBy(
                cb.desc(root.get(TIMESTAMP_CREATED))
        );

        Query<UserTransaction> query = session.createQuery(getAll);
        query.setFirstResult(pageSize * pageNumber);
        query.setMaxResults(pageSize);
        query.setHint(GRAPH_TYPE, entityGraph);
        List<UserTransaction> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void save(UserTransaction userTransaction) {
        if (log.isDebugEnabled()) {
            log.debug("Saving user transaction entity: {}", userTransaction);
        }
        List<Order> orderLine = userTransaction.getOrderLine();
        userTransaction.setOrderLine(null);
        orderLine.forEach(order -> order.setUserTransaction(userTransaction));
        userTransaction.setOrderLine(orderLine);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(userTransaction);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(Long id, UserTransaction updatedFields) {

    }

    @Override
    public void deleteById(Long id) {

    }
}

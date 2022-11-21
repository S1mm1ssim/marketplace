package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PositionDao implements Dao<Position, Long> {

    private final SessionFactory sessionFactory;

    @Value("${default.page.size}")
    private int pageSize;
    private static final String POSITION_GRAPH = "graph.Position.item.company.user";
    private static final String GRAPH_TYPE = "javax.persistence.loadgraph";

    private static final String COMPANY_FIELD_NAME = "company";
    private static final String COMPANY_ID = "id";
    private static final String IS_COMPANY_SOFT_DELETED = "isDeleted";
    private static final String USER_FIELD_NAME = "createdBy";
    private static final String USER_ID = "id";
    private static final String ITEM_FIELD_NAME = "item";
    private static final String ITEM_ID = "id";
    private static final String POSITION_ID = "id";
    private static final String POSITION_AMOUNT = "amount";

    @Override
    public Position get(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching position entity with id {}", id);
        }
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(POSITION_GRAPH);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Position> byId = cb.createQuery(Position.class);
        Root<Position> root = byId.from(Position.class);

        byId.select(root).where(
                cb.and(
                        cb.equal(root.get(ITEM_ID), id),
                        cb.isFalse(root.get(COMPANY_FIELD_NAME).get(IS_COMPANY_SOFT_DELETED))
                )
        );

        Query<Position> query = session.createQuery(byId);
        query.setHint(GRAPH_TYPE, entityGraph);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("Position entity with id {} not found", id);
            throw new EntityNotFoundException(format("Position entity with id=%s is not found.", id), e);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Position> getAll(int pageNumber, Map<String, String> filterProperties) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all positions for page {}", pageNumber);
        }
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(POSITION_GRAPH);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Position> getAll = cb.createQuery(Position.class);
        Root<Position> root = getAll.from(Position.class);

        getAll.select(root).where(
                cb.isFalse(root.get(COMPANY_FIELD_NAME).get(IS_COMPANY_SOFT_DELETED))
        );

        Query<Position> query = session.createQuery(getAll);
        query.setFirstResult(pageSize * pageNumber);
        query.setMaxResults(pageSize);
        query.setHint(GRAPH_TYPE, entityGraph);
        List<Position> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void save(Position position) {
        if (log.isDebugEnabled()) {
            log.debug("Saving position entity: {}", position);
        }
        Session session = sessionFactory.openSession();
        Item item = session.get(Item.class, position.getItem().getId());
        position.setItem(item);
        Transaction transaction = session.beginTransaction();
        session.persist(position);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(Long id, Position updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating position entity with id {} with values from: {}", id, updatedFields);
        }
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        Position position = session.find(Position.class, id, LockModeType.OPTIMISTIC);
        if (updatedFields.getItem().getId() != null) {
            position.getItem().setId(updatedFields.getItem().getId());
        }
        if (updatedFields.getCompany().getId() != null) {
            position.getCompany().setId(updatedFields.getCompany().getId());
        }
        if (updatedFields.getCreatedBy().getId() != null) {
            position.getCreatedBy().setId(updatedFields.getCreatedBy().getId());
        }
        if (updatedFields.getAmount() != null) {
            position.setAmount(updatedFields.getAmount());
        }

        session.merge(position);
        transaction.commit();
        session.close();
    }

    @Override
    public void deleteById(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting position entity with id: {}", id);
        }
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Position> delete = cb.createCriteriaDelete(Position.class);
        Root<Position> root = delete.from(Position.class);
        delete.where(cb.equal(root.get(POSITION_ID), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(delete).executeUpdate();
        transaction.commit();
        session.close();
    }
}

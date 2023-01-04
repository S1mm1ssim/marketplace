package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.utils.Utils;
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

import static com.modsensoftware.marketplace.domain.Position.ID_FIELD_NAME;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class PositionDao implements Dao<Position, Long> {

    private final SessionFactory sessionFactory;
    private final ItemDao itemDao;
    private final UserDao userDao;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.positionNotFound}")
    private String positionNotFoundMessage;

    private static final String POSITION_GRAPH = "graph.Position.item.user";
    private static final String GRAPH_TYPE = "javax.persistence.loadgraph";

    @Override
    public Position get(Long id) {
        log.debug("Fetching position entity with id {}", id);
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(POSITION_GRAPH);
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Position> byId = criteriaBuilder.createQuery(Position.class);
        Root<Position> root = byId.from(Position.class);

        byId.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get(ID_FIELD_NAME), id)));

        Query<Position> query = session.createQuery(byId);
        query.setHint(GRAPH_TYPE, entityGraph);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("Position entity with id {} not found", id);
            throw new EntityNotFoundException(format(positionNotFoundMessage, id), e);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Position> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all positions for page {}", pageNumber);
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(POSITION_GRAPH);
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Position> getAll = criteriaBuilder.createQuery(Position.class);
        Root<Position> root = getAll.from(Position.class);

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
        log.debug("Saving position entity: {}", position);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(position);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(Long id, Position updatedFields) {
        log.debug("Updating position entity with id {} with values from: {}", id, updatedFields);
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            Position position = session.find(Position.class, id, LockModeType.OPTIMISTIC);
            if (updatedFields.getItem().getId() != null) {
                Item updItem = itemDao.get(updatedFields.getItem().getId());
                position.setItem(updItem);
            }
            if (updatedFields.getCreatedBy().getId() != null) {
                User updUser = userDao.get(updatedFields.getCreatedBy().getId());
                position.setCreatedBy(updUser);
            }
            Utils.setIfNotNull(updatedFields.getCompanyId(), position::setCompanyId);
            Utils.setIfNotNull(updatedFields.getAmount(), position::setAmount);
            session.merge(position);
            transaction.commit();
        }
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting position entity with id: {}", id);
        Session session = sessionFactory.openSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaDelete<Position> delete = criteriaBuilder.createCriteriaDelete(Position.class);
        Root<Position> root = delete.from(Position.class);
        delete.where(criteriaBuilder.equal(root.get(ID_FIELD_NAME), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(delete).executeUpdate();
        transaction.commit();
        session.close();
    }
}

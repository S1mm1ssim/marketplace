package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemDao implements Dao<Item, UUID> {

    private final SessionFactory sessionFactory;
    private final CategoryDao categoryDao;

    @Value("${default.page.size}")
    private int pageSize;
    private static final String ITEM_ENTITY_GRAPH = "graph.Item.category.parent";
    private static final String GRAPH_TYPE = "javax.persistence.loadgraph";

    private static final String ITEM_CATEGORY_COLUMN_NAME = "category";
    private static final String ITEM_ID = "id";

    @Override
    public Item get(UUID id) {
        log.debug("Fetching item entity with id {}", id);
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(ITEM_ENTITY_GRAPH);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Item> byId = cb.createQuery(Item.class);
        Root<Item> root = byId.from(Item.class);
        Join<Item, Category> category = root.join(ITEM_CATEGORY_COLUMN_NAME);

        byId.select(root).where(cb.equal(root.get(ITEM_ID), id));

        Query<Item> query = session.createQuery(byId);
        query.setHint(GRAPH_TYPE, entityGraph);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("Item with uuid {} not found", id);
            throw new EntityNotFoundException(format("Item entity with uuid=%s is not present.", id), e);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Item> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all items for page {}", pageNumber);
        Session session = sessionFactory.openSession();
        RootGraph<?> entityGraph = session.getEntityGraph(ITEM_ENTITY_GRAPH);
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Item> getAll = cb.createQuery(Item.class);
        Root<Item> root = getAll.from(Item.class);
        Join<Item, Category> category = root.join(ITEM_CATEGORY_COLUMN_NAME);

        getAll.select(root);

        Query<Item> query = session.createQuery(getAll);
        query.setFirstResult(pageSize * pageNumber);
        query.setMaxResults(pageSize);
        query.setHint(GRAPH_TYPE, entityGraph);
        List<Item> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void save(Item item) {
        log.debug("Saving item entity: {}", item);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(item);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(UUID id, Item updatedFields) {
        log.debug("Updating item entity with id {} with values from: {}", id, updatedFields);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Item item = session.find(Item.class, id, LockModeType.OPTIMISTIC);
            Utils.setIfNotNull(updatedFields.getName(), item::setName);
            Utils.setIfNotNull(updatedFields.getDescription(), item::setDescription);
            if (updatedFields.getCategory().getId() != null) {
                Category updCategory = categoryDao.get(updatedFields.getCategory().getId());
                item.setCategory(updCategory);
            }
            session.merge(item);
        } finally {
            transaction.commit();
            session.close();
        }
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting item entity with id: {}", id);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Item> delete = cb.createCriteriaDelete(Item.class);
        Root<Item> root = delete.from(Item.class);
        delete.where(cb.equal(root.get(ITEM_ID), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(delete).executeUpdate();
        transaction.commit();
        session.close();
    }
}
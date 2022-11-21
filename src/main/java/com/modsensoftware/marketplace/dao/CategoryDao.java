package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryDao implements Dao<Category, Long> {

    private final SessionFactory sessionFactory;

    @Value("${default.page.size}")
    private int pageSize;
    private static final String CATEGORY_PARENT_FIELD_NAME = "parent";
    private static final String PARENT_ID = "id";
    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_NAME = "name";
    private static final String CATEGORY_DESCRIPTION = "description";

    @Override
    public Category get(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching category entity with id {}", id);
        }
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> byId = cb.createQuery(Category.class);
        Root<Category> root = byId.from(Category.class);
        Join<Category, Category> parentCategory =
                root.join(CATEGORY_PARENT_FIELD_NAME, JoinType.LEFT);

        byId.select(root).where(cb.equal(root.get(CATEGORY_ID), id));

        Query<Category> query = session.createQuery(byId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("Category with id {} not found", id);
            throw new EntityNotFoundException(format("Category entity with id=%s is not present.", id), e);
        } finally {
            session.close();
        }
    }


    @Override
    public List<Category> getAll(int pageNumber, Map<String, String> filterProperties) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all categories for page {}", pageNumber);
        }
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Category> getAll = cb.createQuery(Category.class);
        Root<Category> root = getAll.from(Category.class);
        Join<Category, Category> parentCategory =
                root.join(CATEGORY_PARENT_FIELD_NAME, JoinType.LEFT);

        getAll.select(root);

        Query<Category> query = session.createQuery(getAll);
        query.setFirstResult(pageSize * pageNumber);
        query.setMaxResults(pageSize);
        List<Category> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void save(Category category) {
        if (log.isDebugEnabled()) {
            log.debug("Saving category entity: {}", category);
        }
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(category);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(Long id, Category updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating category entity with id {} with values from: {}", id, updatedFields);
        }
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<Category> update = cb.createCriteriaUpdate(Category.class);
        Root<Category> root = update.from(Category.class);

        setIfNotNull(CATEGORY_NAME, updatedFields.getName(), update::set);
        setIfNotNull(CATEGORY_DESCRIPTION, updatedFields.getDescription(), update::set);
        update.set(root.get(CATEGORY_PARENT_FIELD_NAME).get(PARENT_ID),
                updatedFields.getParent() != null
                        ? updatedFields.getParent().getId()
                        : null
        );
        update.where(cb.equal(root.get(CATEGORY_ID), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(update).executeUpdate();
        transaction.commit();
    }

    @Override
    public void deleteById(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting category entity with id: {}", id);
        }
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<Category> delete = cb.createCriteriaDelete(Category.class);
        Root<Category> root = delete.from(Category.class);
        delete.where(cb.equal(root.get(CATEGORY_ID), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(delete).executeUpdate();
        transaction.commit();
    }
}

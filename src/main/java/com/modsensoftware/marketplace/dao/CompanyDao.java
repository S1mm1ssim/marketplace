package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Company;
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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.NAME_FILTER_NAME;
import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;
import static com.modsensoftware.marketplace.utils.Utils.wrapIn;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CompanyDao implements Dao<Company, Long> {

    private final SessionFactory sessionFactory;

    @Value("${default.page.size}")
    private int pageSize;
    private static final String ID_COLUMN_NAME = "id";
    private static final String NAME_COLUMN_NAME = "name";
    private static final String EMAIL_COLUMN_NAME = "email";
    private static final String DESCRIPTION_COLUMN_NAME = "description";
    private static final String IS_SOFT_DELETED = "isDeleted";

    @Override
    public Company get(Long id) {
        log.debug("Fetching company entity with id {}", id);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Company> byId = cb.createQuery(Company.class);
        Root<Company> root = byId.from(Company.class);
        byId.select(root).where(
                cb.and(
                        cb.equal(root.get(ID_COLUMN_NAME), id),
                        cb.isFalse(root.get(IS_SOFT_DELETED))
                )
        );

        Query<Company> query = session.createQuery(byId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("Company entity with id {} not found", id);
            throw new EntityNotFoundException(format("Company entity with id=%s is not present.", id), e);
        } finally {
            session.close();
        }
    }

    public boolean existsByEmail(String email) {
        log.debug("Checking if company with email {} exists", email);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Company> byId = cb.createQuery(Company.class);
        Root<Company> root = byId.from(Company.class);
        byId.select(root).where(
                cb.and(
                        cb.equal(root.get(EMAIL_COLUMN_NAME), email),
                        cb.isFalse(root.get(IS_SOFT_DELETED))
                )
        );

        Query<Company> query = session.createQuery(byId);
        try {
            // If the company with provided email
            // does not exist the exception will be thrown
            query.getSingleResult();
            return true;
        } catch (NoResultException e) {
            log.info("Company entity with email {} not found", email);
            return false;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Company> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all companies for page {}", pageNumber);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Company> getAll = cb.createQuery(Company.class);
        Root<Company> root = getAll.from(Company.class);

        // Filtering
        List<Predicate> predicates
                = constructPredicatesFromProps(filterProperties, cb, root);
        predicates.add(cb.isFalse(root.get(IS_SOFT_DELETED)));
        getAll.select(root).where(predicates.toArray(new Predicate[0]));

        // Paging
        Query<Company> query = session.createQuery(getAll);
        query.setFirstResult(pageSize * pageNumber);
        query.setMaxResults(pageSize);
        List<Company> results = query.getResultList();
        session.close();
        return results;
    }

    @Override
    public void save(Company company) {
        log.debug("Saving company entity: {}", company);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(company);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(Long id, Company updatedFields) {
        log.debug("Updating company entity with id {} with values from: {}", id, updatedFields);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<Company> update = cb.createCriteriaUpdate(Company.class);
        Root<Company> root = update.from(Company.class);

        int totalFieldsUpdated = 0;
        if (setIfNotNull(NAME_COLUMN_NAME, updatedFields.getName(), update::set)) {
            totalFieldsUpdated++;
        }
        if (setIfNotNull(EMAIL_COLUMN_NAME, updatedFields.getEmail(), update::set)) {
            totalFieldsUpdated++;
        }
        if (setIfNotNull(DESCRIPTION_COLUMN_NAME, updatedFields.getDescription(), update::set)) {
            totalFieldsUpdated++;
        }
        if (totalFieldsUpdated > 0) {
            update.where(
                    cb.and(
                            cb.equal(root.get(ID_COLUMN_NAME), id),
                            cb.isFalse(root.get(IS_SOFT_DELETED))
                    )
            );

            Transaction transaction = session.beginTransaction();
            session.createQuery(update).executeUpdate();
            transaction.commit();
        }
        session.close();
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting company entity with id: {}", id);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<Company> softDelete = cb.createCriteriaUpdate(Company.class);
        Root<Company> root = softDelete.from(Company.class);
        softDelete.set(IS_SOFT_DELETED, true);
        softDelete.where(cb.equal(root.get(ID_COLUMN_NAME), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(softDelete).executeUpdate();
        transaction.commit();
    }

    private List<Predicate> constructPredicatesFromProps(
            Map<String, String> filterProperties,
            CriteriaBuilder cb, Root<Company> root) {
        List<Predicate> predicates = new ArrayList<>();
        filterProperties.forEach((key, value) -> {
            if (key.equals(EMAIL_FILTER_NAME)) {
                predicates.add(cb.like(root.get(EMAIL_COLUMN_NAME), wrapIn(value, "%")));
            } else if (key.equals(NAME_FILTER_NAME)) {
                predicates.add(cb.like(root.get(NAME_COLUMN_NAME), wrapIn(value, "%")));
            }
        });
        return predicates;
    }
}

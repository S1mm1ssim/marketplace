package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.InvalidFilterException;
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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.modsensoftware.marketplace.constants.Constants.COMPANY_ID_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.CREATED_BETWEEN_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.NAME_FILTER_NAME;
import static com.modsensoftware.marketplace.domain.User.COMPANY_ID_FIELD_NAME;
import static com.modsensoftware.marketplace.domain.User.CREATED_FIELD_NAME;
import static com.modsensoftware.marketplace.domain.User.EMAIL_FIELD_NAME;
import static com.modsensoftware.marketplace.domain.User.FULL_NAME_FIELD_NAME;
import static com.modsensoftware.marketplace.domain.User.UPDATED_FIELD_NAME;
import static com.modsensoftware.marketplace.domain.User.USERNAME_FIELD_NAME;
import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;
import static com.modsensoftware.marketplace.utils.Utils.wrapIn;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDao implements Dao<User, UUID> {

    private final SessionFactory sessionFactory;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.userNotFound}")
    private String userNotFoundMessage;
    @Value("${exception.message.invalidCreatedBetweenFilter}")
    private String invalidCreatedBetweenFilterMessage;

    private static final String CREATED_BETWEEN_DELIMITER = ",";
    private static final int TIMESTAMPS_AMOUNT_EXPECTED_IN_FILTER = 2;

    @Override
    public User get(UUID id) {
        log.debug("Fetching user entity with uuid {}", id);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> byId = cb.createQuery(User.class);
        Root<User> root = byId.from(User.class);

        byId.select(root).where(
                cb.and(cb.equal(root.get(User.ID_FIELD_NAME), id))
        );

        Query<User> query = session.createQuery(byId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            log.error("User entity with uuid {} not found", id);
            throw new EntityNotFoundException(format(userNotFoundMessage, id), e);
        } finally {
            session.close();
        }
    }

    @Override
    public List<User> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all users for page {}", pageNumber);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<User> getAll = cb.createQuery(User.class);
        Root<User> root = getAll.from(User.class);

        List<Predicate> predicates = constructPredicatesFromProps(filterProperties, cb, root);
        getAll.select(root).where(predicates.toArray(new Predicate[0]));

        Query<User> query = session.createQuery(getAll);
        query.setFirstResult(pageSize * pageNumber);
        query.setMaxResults(pageSize);
        List<User> result = query.getResultList();
        session.close();
        return result;
    }

    @Override
    public void save(User user) {
        log.debug("Saving user entity: {}", user);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.persist(user);
        transaction.commit();
        session.close();
    }

    @Override
    public void update(UUID id, User updatedFields) {
        log.debug("Updating user entity with id {} with values from: {}", id, updatedFields);
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaUpdate<User> update = cb.createCriteriaUpdate(User.class);
            Root<User> root = update.from(User.class);

            int totalFieldsUpdated = 0;
            if (setIfNotNull(USERNAME_FIELD_NAME, updatedFields.getUsername(), update::set)) {
                totalFieldsUpdated++;
            }
            if (setIfNotNull(EMAIL_FIELD_NAME, updatedFields.getEmail(), update::set)) {
                totalFieldsUpdated++;
            }
            if (setIfNotNull(FULL_NAME_FIELD_NAME, updatedFields.getName(), update::set)) {
                totalFieldsUpdated++;
            }
            if (setIfNotNull(COMPANY_ID_FIELD_NAME, updatedFields.getCompanyId(), update::set)) {
                totalFieldsUpdated++;
            }
            if (totalFieldsUpdated > 0) {
                setIfNotNull(UPDATED_FIELD_NAME, updatedFields.getUpdated(), update::set);

                update.where(cb.equal(root.get(User.ID_FIELD_NAME), id));

                Transaction transaction = session.beginTransaction();
                session.createQuery(update).executeUpdate();
                transaction.commit();
            }
        }
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting user entity with id: {}", id);
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<User> delete = cb.createCriteriaDelete(User.class);
        Root<User> root = delete.from(User.class);
        delete.where(cb.equal(root.get(User.ID_FIELD_NAME), id));

        Transaction transaction = session.beginTransaction();
        session.createQuery(delete).executeUpdate();
        transaction.commit();
    }

    private List<Predicate> constructPredicatesFromProps(
            Map<String, String> filterProperties,
            CriteriaBuilder cb, Root<User> root) {
        List<Predicate> predicates = new ArrayList<>();
        filterProperties.forEach((key, value) -> {
            if (key.equals(EMAIL_FILTER_NAME)) {
                predicates.add(cb.like(root.get(EMAIL_FIELD_NAME), wrapIn(value, "%")));
            } else if (key.equals(NAME_FILTER_NAME)) {
                predicates.add(cb.like(root.get(FULL_NAME_FIELD_NAME), wrapIn(value, "%")));
            } else if (key.equals(CREATED_BETWEEN_FILTER_NAME)) {
                Map.Entry<String, String> borders = parseCreatedBetween(value);
                predicates.add(cb.between(root.get(CREATED_FIELD_NAME),
                        LocalDateTime.parse(borders.getKey()),
                        LocalDateTime.parse(borders.getValue())));
            } else if (key.equals(COMPANY_ID_FILTER_NAME)) {
                predicates.add(cb.equal(root.get(COMPANY_ID_FIELD_NAME), Long.parseLong(value)));
            }
        });
        return predicates;
    }

    private Map.Entry<String, String> parseCreatedBetween(String createdBetween) {
        Map<String, String> borders = new HashMap<>();
        String[] parsed = createdBetween.split(CREATED_BETWEEN_DELIMITER);
        if (parsed.length != TIMESTAMPS_AMOUNT_EXPECTED_IN_FILTER) {
            log.error("Provided filter: {} is invalid", createdBetween);
            log.debug("Timestamps provided: {}, expected: {}", parsed.length, TIMESTAMPS_AMOUNT_EXPECTED_IN_FILTER);
            throw new InvalidFilterException(format(invalidCreatedBetweenFilterMessage, createdBetween));
        }
        borders.put(parsed[0], parsed[1]);
        return borders.entrySet().iterator().next();
    }
}

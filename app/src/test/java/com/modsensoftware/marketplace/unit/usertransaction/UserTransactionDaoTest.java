package com.modsensoftware.marketplace.unit.usertransaction;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Order;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.unit.AbstractDaoTest;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
public class UserTransactionDaoTest extends AbstractDaoTest {

    @Autowired
    private UserDao userDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private PositionDao positionDao;

    @Autowired
    private UserTransactionDao underTest;

    @Value("${default.page.size}")
    private int pageSize;

    @Test
    public void canSaveUserTransaction() {
        // given
        Position savedPosition = generateDefaultPosition();
        Position orderPosition = generateOrderPosition(savedPosition.getId(), 0L);
        UserTransaction userTransaction = generateUserTransaction(savedPosition.getCreatedBy().getId(),
                orderPosition);

        // when
        underTest.save(userTransaction);

        // then
        Assertions.assertThat(userTransaction.getId()).isNotNull();
        userTransaction.getOrderLine().forEach(order1 -> {
            Assertions.assertThat(order1.getId()).isNotNull();
            Assertions.assertThat(order1.getUserTransaction()).isNotNull();
            Assertions.assertThat(order1.getPosition()).isNotNull();
        });

        // clean up
        deleteUserTransaction(userTransaction);
        deletePositionAndItsEntities(savedPosition);
    }

    @Test
    @DisplayName("Saving UserTransaction with orders' position.version having null values "
            + "should throw org.hibernate.exception.ConstraintViolationException with message "
            + "ERROR: null value in column \"position_id\" of relation \"order\" violates not-null constraint")
    public void userTransactionSaveShouldFailWithConstraintViolationException() {
        // given
        Position savedPosition = generateDefaultPosition();
        Position orderPosition = generateOrderPosition(savedPosition.getId(), null);
        UserTransaction userTransaction = generateUserTransaction(savedPosition.getCreatedBy().getId(),
                orderPosition);
        final String exceptionMessageExpected = "ERROR: null value in column \"position_id\" of "
                + "relation \"order\" violates not-null constraint";

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.save(userTransaction))
                .isInstanceOf(PersistenceException.class)
                .getCause().isInstanceOf(ConstraintViolationException.class)
                .getCause().isInstanceOf(PSQLException.class)
                .hasMessageContaining(exceptionMessageExpected);

        // clean up
        deletePositionAndItsEntities(savedPosition);
    }

    @Test
    public void canGetAllTransactionsForUserWithPagination() {
        // given
        User user1 = generatePersistentUser("customer1@email.com", "username3", 1L);
        User user2 = generatePersistentUser("customer2@email.com", "username4", 2L);

        Position savedPosition = generateDefaultPosition();
        List<UserTransaction> userTransactions = generateTransactionsForUsers(savedPosition, user1, user2);

        UUID userId = user1.getId();
        Map<String, String> filterProps = Map.of("userId", userId.toString());

        // when
        List<UserTransaction> firstPage = underTest.getAll(0, filterProps);
        List<UserTransaction> secondPage = underTest.getAll(1, filterProps);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize);
        firstPage.forEach(transaction -> Assertions.assertThat(transaction.getUserId()).isEqualTo(userId));
        Assertions.assertThat(secondPage.size()).isEqualTo(userTransactions.size() / 2 - pageSize);
        secondPage.forEach(transaction -> Assertions.assertThat(transaction.getUserId()).isEqualTo(userId));

        // clean up
        userTransactions.forEach(this::deleteUserTransaction);
        deleteEntities(user1, user2);
        deletePositionAndItsEntities(savedPosition);
    }

    private User generatePersistentUser(String email, String username, Long companyId) {
        User user = new User(UUID.randomUUID(), username, email, "full name",
                now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), companyId);
        userDao.save(user);
        return user;
    }

    private Position generateDefaultPosition() {
        Long companyId = 1L;
        User user = generatePersistentUser("email@email.com", "username1", companyId);
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, 1L);
        itemDao.save(item);
        Position position = new Position(null, item, companyId, user, now().truncatedTo(SECONDS), 120d, 0.1d, 0L);
        positionDao.save(position);
        return position;
    }

    private Position generateOrderPosition(Long positionId, Long positionVersion) {
        return Position.builder().id(positionId).version(positionVersion).build();
    }

    private UserTransaction generateUserTransaction(UUID userId, Position orderPosition) {
        return new UserTransaction(null, userId, now().truncatedTo(SECONDS), List.of(
                new Order(null, 5d, orderPosition, null))
        );
    }

    private void deletePositionAndItsEntities(Position position) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(position);
        session.delete(position.getItem());
        session.delete(position.getItem().getCategory());
        session.delete(position.getCreatedBy());
        transaction.commit();
        session.close();
    }

    private void deleteUserTransaction(UserTransaction userTransaction) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        userTransaction.getOrderLine().forEach(session::delete);
        session.delete(userTransaction);
        transaction.commit();
        session.close();
    }

    private void deleteEntities(Object... entities) {
        List<Object> entityList = List.of(entities);
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        entityList.forEach(session::delete);
        transaction.commit();
        session.close();
    }

    private List<UserTransaction> generateTransactionsForUsers(Position savedPosition, User user1, User user2) {
        Position orderPosition = generateOrderPosition(savedPosition.getId(), savedPosition.getVersion());
        List<UserTransaction> userTransactions = new ArrayList<>();
        // Generating pageSize + 1 userTransactions for each user
        for (int i = 0; i < pageSize + 1; i++) {
            UserTransaction userTransaction1 = generateUserTransaction(user1.getId(), orderPosition);
            UserTransaction userTransaction2 = generateUserTransaction(user2.getId(), orderPosition);
            userTransactions.add(userTransaction1);
            userTransactions.add(userTransaction2);
            underTest.save(userTransaction1);
            underTest.save(userTransaction2);
        }
        return userTransactions;
    }
}

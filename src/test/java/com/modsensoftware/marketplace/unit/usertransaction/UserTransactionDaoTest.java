package com.modsensoftware.marketplace.unit.usertransaction;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Order;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.enums.Role;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserTransactionDaoTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CompanyDao companyDao;

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

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    @Value("${default.page.size}")
    private int pageSize;

    @Test
    public void canSaveUserTransaction() {
        // given
        Position savedPosition = generatePosition();

        Position transactionPosition = new Position();
        transactionPosition.setId(savedPosition.getId());
        transactionPosition.setVersion(0L);

        UserTransaction userTransaction = new UserTransaction(
                null,
                savedPosition.getCreatedBy().getId(),
                now().truncatedTo(SECONDS),
                List.of(new Order(null, 5d, transactionPosition, null))
        );

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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        userTransaction.getOrderLine().forEach(session::delete);
        session.delete(userTransaction);
        deletePositionAndItsEntities(session, savedPosition);

        transaction.commit();
        session.close();
    }

    @DisplayName("Saving UserTransaction with orders' position.version having null values "
            + "should throw org.hibernate.exception.ConstraintViolationException with message "
            + "ERROR: null value in column \"position_id\" of relation \"order\" violates not-null constraint")
    @Test
    public void userTransactionSaveShouldFailWithConstraintViolationException() {
        // given
        Position savedPosition = generatePosition();

        Position transactionPosition = new Position();
        transactionPosition.setId(savedPosition.getId());
        // Null version of position
        transactionPosition.setVersion(null);

        UserTransaction userTransaction = new UserTransaction(
                null,
                savedPosition.getCreatedBy().getId(),
                now().truncatedTo(SECONDS),
                List.of(new Order(null, 5d, transactionPosition, null))
        );

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.save(userTransaction))
                .isInstanceOf(PersistenceException.class)
                .getCause().isInstanceOf(ConstraintViolationException.class)
                .getCause().isInstanceOf(PSQLException.class)
                .hasMessageContaining("ERROR: null value in column \"position_id\" of "
                        + "relation \"order\" violates not-null constraint");

        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        deletePositionAndItsEntities(session, savedPosition);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllTransactionsForUserWithPagination() {
        // given
        Company company = new Company(null, "company", "customerCompany@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user1 = new User(null, "username", "customer1@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        User user2 = new User(null, "username", "customer2@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        userDao.save(user1);
        userDao.save(user2);

        Position savedPosition = generatePosition();
        Position transactionPosition = new Position();
        transactionPosition.setId(savedPosition.getId());
        transactionPosition.setVersion(savedPosition.getVersion());

        List<UserTransaction> userTransactions
                = generatePersistentTransactionsForUsers(transactionPosition, user1, user2);

        UUID userId = user1.getId();
        Map<String, String> filterProps = new HashMap<>();
        filterProps.put("userId", userId.toString());
        int pageNumber = 0;

        // when
        List<UserTransaction> firstPage = underTest.getAll(pageNumber, filterProps);
        List<UserTransaction> secondPage = underTest.getAll(pageNumber + 1, filterProps);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize);
        firstPage.forEach(transaction -> Assertions.assertThat(transaction.getUserId()).isEqualTo(userId));
        Assertions.assertThat(secondPage.size()).isEqualTo(userTransactions.size() / 2 - pageSize);
        secondPage.forEach(transaction -> Assertions.assertThat(transaction.getUserId()).isEqualTo(userId));

        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        userTransactions.forEach(userTransaction -> {
            userTransaction.getOrderLine().forEach(session::delete);
            session.delete(userTransaction);
        });
        deletePositionAndItsEntities(session, savedPosition);
        session.delete(user1);
        session.delete(user2);
        session.delete(company);
        transaction.commit();
        session.close();
    }

    private Position generatePosition() {
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.STORAGE_MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        userDao.save(user);
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, 1L);
        itemDao.save(item);
        Position position = new Position(null, item, company, user, now().truncatedTo(SECONDS), 120d, 0.1d, 0L);
        positionDao.save(position);
        return position;
    }

    private void deletePositionAndItsEntities(Session sessionWithTransaction, Position position) {
        sessionWithTransaction.delete(position);
        sessionWithTransaction.delete(position.getItem());
        sessionWithTransaction.delete(position.getItem().getCategory());
        sessionWithTransaction.delete(position.getCreatedBy());
        sessionWithTransaction.delete(position.getCompany());
    }

    private List<UserTransaction> generatePersistentTransactionsForUsers(
            Position transactionPosition, User user1, User user2) {

        List<UserTransaction> userTransactions = new ArrayList<>();

        // Generating pageSize + 1 userTransactions for each user
        for (int i = 0; i < pageSize + 1; i++) {
            UserTransaction userTransaction1 = new UserTransaction(
                    null, user1.getId(), now().truncatedTo(SECONDS),
                    List.of(new Order(null, 3d, transactionPosition, null)));
            UserTransaction userTransaction2 = new UserTransaction(
                    null, user2.getId(), now().truncatedTo(SECONDS),
                    List.of(new Order(null, 3d, transactionPosition, null)));
            userTransactions.add(userTransaction1);
            userTransactions.add(userTransaction2);
            underTest.save(userTransaction1);
            underTest.save(userTransaction2);
        }
        return userTransactions;
    }
}

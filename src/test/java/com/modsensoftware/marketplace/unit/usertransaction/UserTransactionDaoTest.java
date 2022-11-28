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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        session.delete(savedPosition);
        session.delete(savedPosition.getItem());
        session.delete(savedPosition.getItem().getCategory());
        session.delete(savedPosition.getCreatedBy());
        session.delete(savedPosition.getCompany());

        transaction.commit();
        session.close();
    }

    private Position generatePosition() {
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
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
}

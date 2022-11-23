package com.modsensoftware.marketplace.unit.user;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.enums.Role;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.InvalidFilterException;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserDaoTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private UserDao underTest;

    @Autowired
    private CompanyDao companyDao;

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    @Value("${default.page.size}")
    private int pageSize;

    @Test
    public void canSaveUser() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);

        // when
        underTest.save(user);

        // then
        User result = underTest.get(user.getId());
        Assertions.assertThat(result).isEqualTo(user);

        // clean up
        underTest.deleteById(user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetUserByUuid() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        underTest.save(user);
        // when
        User result = underTest.get(user.getId());
        // then
        Assertions.assertThat(result).isEqualTo(user);
        // clean up
        underTest.deleteById(user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void getByNonExistentUuidShouldThrowEntityNotFoundException() {
        // given
        String nonExistentUuid = "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d";
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(UUID.fromString(nonExistentUuid)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format("User entity with uuid=%s is not found.", nonExistentUuid));
    }

    @Test
    public void canGetAllNonSoftDeletedUsersWithPagination() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        Company softDeletedCompany = new Company(null, "company", "softDeleted@company.com",
                now().truncatedTo(SECONDS), "description", true);
        companyDao.save(company);
        companyDao.save(softDeletedCompany);
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize + 1; i++) {
            User user = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
            User softDeletedUser = new User(null, "username",
                    format("email%s@email.com", random.nextInt()), "full name", Role.MANAGER,
                    now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), softDeletedCompany);
            users.add(user);
            users.add(softDeletedUser);
            underTest.save(user);
            underTest.save(softDeletedUser);
        }

        // when
        List<User> firstPage = underTest.getAll(0, Collections.emptyMap());
        List<User> secondPage = underTest.getAll(1, Collections.emptyMap());

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize);
        Assertions.assertThat(firstPage).noneMatch(user -> user.getCompany().getIsDeleted().equals(true));
        Assertions.assertThat(secondPage.size()).isEqualTo(users.size() / 2 - pageSize);
        Assertions.assertThat(secondPage).noneMatch(user -> user.getCompany().getIsDeleted().equals(true));
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        users.forEach(session::delete);
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllUsersFilteredByName() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
            User anotherUser = new User(null, "username", format("email%s@email.com", random.nextInt()), "name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
            users.add(user);
            users.add(anotherUser);
            underTest.save(user);
            underTest.save(anotherUser);
        }

        // when
        Map<String, String> nameFilter = new HashMap<>();
        String filterValue = "full";
        nameFilter.put("name", filterValue);
        List<User> firstPage = underTest.getAll(0, nameFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize / 2);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        users.forEach(session::delete);
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllUsersFilteredByEmail() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
            User anotherUser = new User(null, "username", format("user%s@user.com", random.nextInt()), "full name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
            users.add(user);
            users.add(anotherUser);
            underTest.save(user);
            underTest.save(anotherUser);
        }

        // when
        Map<String, String> emailFilter = new HashMap<>();
        String filterValue = "email";
        emailFilter.put("email", filterValue);
        List<User> firstPage = underTest.getAll(0, emailFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize / 2);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        users.forEach(session::delete);
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllUsersFilteredByCompanyId() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        Company anotherCompany = new Company(null, "company", "anotherCompany@anotherCompany.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        companyDao.save(anotherCompany);
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
            User anotherUser = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                    Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), anotherCompany);
            users.add(user);
            users.add(anotherUser);
            underTest.save(user);
            underTest.save(anotherUser);
        }

        // when
        Map<String, String> companyIdFilter = new HashMap<>();
        String filterValue = String.valueOf(company.getId());
        companyIdFilter.put("companyId", filterValue);
        List<User> firstPage = underTest.getAll(0, companyIdFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize / 2);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        users.forEach(session::delete);
        session.delete(company);
        session.delete(anotherCompany);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllUsersFilteredByTimestampCreated() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        Random random = new Random();
        User user1 = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                Role.MANAGER, LocalDateTime.parse("2022-11-01T12:00:00"), LocalDateTime.parse("2022-11-01T12:00:00"), company);
        User user2 = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                Role.MANAGER, LocalDateTime.parse("2022-11-07T12:00:00"), LocalDateTime.parse("2022-11-07T12:00:00"), company);
        User user3 = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                Role.MANAGER, LocalDateTime.parse("2022-10-01T12:00:00"), LocalDateTime.parse("2022-10-01T12:00:00"), company);
        User user4 = new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                Role.MANAGER, LocalDateTime.parse("2022-11-18T12:00:00"), LocalDateTime.parse("2022-11-18T12:00:00"), company);
        List<User> users = List.of(user1, user2, user3, user4);
        Session saveSession = sessionFactory.openSession();
        Transaction saveTransaction = saveSession.beginTransaction();
        users.forEach(saveSession::save);
        saveTransaction.commit();
        saveSession.close();
        int usersAmountAfterFilter = 2;

        // when
        Map<String, String> createdBetweenFilter = new HashMap<>();
        String filterValue = "2022-11-04T12:00:00,2022-11-18T12:00:00";
        createdBetweenFilter.put("created", filterValue);
        List<User> firstPage = underTest.getAll(0, createdBetweenFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(usersAmountAfterFilter);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        users.forEach(session::delete);
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    @DisplayName("Should throw InvalidFilterException if 'created' filter contains separator amount > 1")
    public void shouldThrowInvalidFilterExceptionDuringFilterByCreatedMoreSeparatorsThanNeeded() {
        // given
        Map<String, String> createdBetweenFilter = new HashMap<>();
        String filterValue = "2022-11-04T12:00:00,,2022-11-18T12:00:00";
        createdBetweenFilter.put("created", filterValue);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.getAll(0, createdBetweenFilter))
                .isInstanceOf(InvalidFilterException.class)
                .hasMessage(format("Filter 'created' = %s is invalid", filterValue));
    }

    @Test
    @DisplayName("Should throw InvalidFilterException if 'created' filter doesn't contain a specified separator")
    public void shouldThrowInvalidFilterExceptionDuringFilterByCreatedNoSeparatorProvided() {
        // given
        Map<String, String> createdBetweenFilter = new HashMap<>();
        String filterValue = "2022-11-04T12:00:00|2022-11-18T12:00:00";
        createdBetweenFilter.put("created", filterValue);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.getAll(0, createdBetweenFilter))
                .isInstanceOf(InvalidFilterException.class)
                .hasMessage(format("Filter 'created' = %s is invalid", filterValue));
    }

    @Test
    public void canUpdateUser() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        Company anotherCompany = new Company(null, "another company", "anotherCompany@anotherCompany.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        companyDao.save(anotherCompany);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        underTest.save(user);
        User updatedFields = new User(null, "upd username", "updEmail@email.com",
                "upd full name", null, null, now().truncatedTo(SECONDS), anotherCompany);
        User expected = new User(user.getId(), updatedFields.getUsername(), updatedFields.getEmail(),
                updatedFields.getName(), user.getRole(), user.getCreated(),
                updatedFields.getUpdated(), updatedFields.getCompany());
        // when
        underTest.update(user.getId(), updatedFields);
        // then
        User result = underTest.get(user.getId());
        Assertions.assertThat(result).isEqualTo(expected);
        // clean up
        underTest.deleteById(user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        session.delete(anotherCompany);
        transaction.commit();
        session.close();
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        underTest.save(user);
        User updatedFields = new User();
        updatedFields.setCompany(new Company());
        // when
        underTest.update(user.getId(), updatedFields);
        // then
        User result = underTest.get(user.getId());
        Assertions.assertThat(result).isEqualTo(user);
        // clean up
        underTest.deleteById(user.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canDeleteByUuid() {
        // given
        Company company = new Company(null, "company", "company@company.com", now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        User user = new User(null, "username", "email@email.com", "full name",
                Role.MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        underTest.save(user);
        // when
        underTest.deleteById(user.getId());
        // then
        Session session = sessionFactory.openSession();
        User result = session.get(User.class, user.getId());
        Assertions.assertThat(result).isNull();
        session.close();
        // clean up
        Session companyDelete = sessionFactory.openSession();
        Transaction transaction = companyDelete.beginTransaction();
        companyDelete.delete(company);
        transaction.commit();
        companyDelete.close();
    }
}

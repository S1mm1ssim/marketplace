package com.modsensoftware.marketplace.unit.user;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.InvalidFilterException;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.modsensoftware.marketplace.enums.Role.MANAGER;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
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
    @Value("${exception.message.userNotFound}")
    private String userNotFoundMessage;
    @Value("${exception.message.invalidCreatedBetweenFilter}")
    private String invalidCreatedBetweenFilterMessage;

    @Test
    public void canSaveAndGetUserByUuid() {
        // given
        User user = generateDefaultTestUser();

        // when
        underTest.save(user);

        // then
        User result = underTest.get(user.getId());
        Assertions.assertThat(result).isEqualTo(user);

        // clean up
        deleteUser(user);
    }

    @Test
    public void getByNonExistentUuidShouldThrowEntityNotFoundException() {
        // given
        String nonExistentUuid = "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d";
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(UUID.fromString(nonExistentUuid)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format(userNotFoundMessage, nonExistentUuid));
    }

    @Test
    public void canGetAllNonSoftDeletedUsersWithPagination() {
        // given
        Company company = generateDefaultTestPersistentCompany();
        Company softDeletedCompany = new Company(null, "company", "softDeleted@company.com",
                now().truncatedTo(SECONDS), "description", true);
        companyDao.save(softDeletedCompany);
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize + 1; i++) {
            User user = generateUserWithRandomEmail(random, company);
            User softDeletedUser = generateUserWithRandomEmail(random, softDeletedCompany);
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
        deleteAllUsers(users);
        deleteCompany(company);
        deleteCompany(softDeletedCompany);
    }

    @Test
    public void canGetAllUsersFilteredByName() {
        // given
        Company company = generateDefaultTestPersistentCompany();
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = generateUserWithRandomEmailAndName(random, company, "full name");
            User anotherUser = generateUserWithRandomEmailAndName(random, company, "name");
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
        deleteAllUsers(users);
        deleteCompany(company);
    }

    @Test
    public void canGetAllUsersFilteredByEmail() {
        // given
        Company company = generateDefaultTestPersistentCompany();
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = generateUserWithRandomizedEmail(random, company, "email%s@email.com");
            User anotherUser = generateUserWithRandomizedEmail(random, company, "user%s@user.com");
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
        deleteAllUsers(users);
        deleteCompany(company);
    }

    @Test
    public void canGetAllUsersFilteredByCompanyId() {
        // given
        Company company = generateDefaultTestPersistentCompany();
        Company anotherCompany = new Company(null, "company", "anotherCompany@anotherCompany.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(anotherCompany);
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = generateUserWithRandomEmail(random, company);
            User anotherUser = generateUserWithRandomEmail(random, anotherCompany);
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
        deleteAllUsers(users);
        deleteCompany(company);
        deleteCompany(anotherCompany);
    }

    @Test
    public void canGetAllUsersFilteredByTimestampCreated() {
        // given
        Company company = generateDefaultTestPersistentCompany();
        List<User> users = generatePersistentUsersForTimestampFilterTest(company);
        int expectedAmountOfUsersLeftAfterFiltering = 2;

        // when
        Map<String, String> createdBetweenFilter = new HashMap<>();
        String filterValue = "2022-11-04T12:00:00,2022-11-18T12:00:00";
        createdBetweenFilter.put("created", filterValue);
        List<User> firstPage = underTest.getAll(0, createdBetweenFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(expectedAmountOfUsersLeftAfterFiltering);

        // clean up
        deleteAllUsers(users);
        deleteCompany(company);
    }

    @ValueSource(strings = {
            "2022-11-04T12:00:00,,2022-11-18T12:00:00",
            "2022-11-04T12:00:00|2022-11-18T12:00:00",
            "2022-11-04T12:00:00,2022-11-18T12:00:00,2022-11-22T12:00:00",
            "2022-11-04T12:00:00"
    })
    @ParameterizedTest
    @DisplayName("Should throw InvalidFilterException if provided filter contains wrong separator or too many timestamps")
    public void shouldThrowInvalidFilterException(String filterValue) {
        // given
        Map<String, String> createdBetweenFilter = new HashMap<>();
        createdBetweenFilter.put("created", filterValue);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.getAll(0, createdBetweenFilter))
                .isInstanceOf(InvalidFilterException.class)
                .hasMessage(format(invalidCreatedBetweenFilterMessage, filterValue));
    }

    @Test
    public void canUpdateUser() {
        // given
        Company company = generateDefaultTestPersistentCompany();
        Company anotherCompany = new Company(null, "another company", "anotherCompany@anotherCompany.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(anotherCompany);
        User user = new User(null, "username", "email@email.com", "full name",
                MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
        underTest.save(user);
        User updatedFields = new User(null, "upd username", "updEmail@email.com",
                "upd full name", null, null, now().truncatedTo(SECONDS), anotherCompany);
        User expected = new User(user.getId(), updatedFields.getUsername(), updatedFields.getEmail(),
                updatedFields.getName(), user.getRole(), user.getCreated(),
                updatedFields.getUpdated(), updatedFields.getCompany());

        // when
        underTest.update(user.getId(), updatedFields);

        // then
        User actual = underTest.get(user.getId());
        Assertions.assertThat(actual).isEqualTo(expected);

        // clean up
        deleteUser(actual);
        deleteCompany(company);
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        User user = generateDefaultTestUser();
        underTest.save(user);
        User updatedFields = new User();
        updatedFields.setCompany(new Company());

        // when
        underTest.update(user.getId(), updatedFields);

        // then
        User result = underTest.get(user.getId());
        Assertions.assertThat(result).isEqualTo(user);

        // clean up
        deleteUser(user);
    }

    @Test
    public void canDeleteByUuid() {
        // given
        User user = generateDefaultTestUser();
        underTest.save(user);

        // when
        underTest.deleteById(user.getId());

        // then
        Session session = sessionFactory.openSession();
        User result = session.get(User.class, user.getId());
        Assertions.assertThat(result).isNull();
        session.close();

        // clean up
        deleteCompany(user.getCompany());
    }

    private Company generateDefaultTestPersistentCompany() {
        Company company = new Company(null, "company", "company@company.com",
                now().truncatedTo(SECONDS), "description", false);
        companyDao.save(company);
        return company;
    }

    private User generateDefaultTestUser() {
        Company company = generateDefaultTestPersistentCompany();
        return new User(null, "username", "email@email.com", "full name",
                MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
    }

    private void deleteUser(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(user);
        session.delete(user.getCompany());
        transaction.commit();
        session.close();
    }

    private void deleteAllUsers(Collection<User> users) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        users.forEach(session::delete);
        transaction.commit();
        session.close();
    }

    private void deleteCompany(Company company) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    private List<User> generatePersistentUsersForTimestampFilterTest(Company userCompany) {
        Random random = new Random();
        User user1 = generateUserWithRandomEmailAndTimestamp(random, userCompany, "2022-10-01T12:00:00");
        User user2 = generateUserWithRandomEmailAndTimestamp(random, userCompany, "2022-11-01T12:00:00");
        User user3 = generateUserWithRandomEmailAndTimestamp(random, userCompany, "2022-11-07T12:00:00");
        User user4 = generateUserWithRandomEmailAndTimestamp(random, userCompany, "2022-11-18T12:00:00");
        List<User> users = List.of(user1, user2, user3, user4);
        Session saveSession = sessionFactory.openSession();
        Transaction saveTransaction = saveSession.beginTransaction();
        users.forEach(saveSession::save);
        saveTransaction.commit();
        saveSession.close();
        return users;
    }

    private User generateUserWithRandomEmail(Random random, Company company) {
        return new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
    }

    private User generateUserWithRandomEmailAndName(Random random, Company company, String name) {
        return new User(null, "username", format("email%s@email.com", random.nextInt()), name,
                MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
    }

    private User generateUserWithRandomEmailAndTimestamp(Random random, Company company, String timestamp) {
        return new User(null, "username", format("email%s@email.com", random.nextInt()), "full name",
                MANAGER, parse(timestamp), parse(timestamp), company);
    }

    private User generateUserWithRandomizedEmail(Random random, Company company, String email) {
        return new User(null, "username", format(email, random.nextInt()), "full name",
                MANAGER, now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
    }
}

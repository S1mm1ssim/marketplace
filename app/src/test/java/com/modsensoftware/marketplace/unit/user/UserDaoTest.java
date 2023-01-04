package com.modsensoftware.marketplace.unit.user;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.InvalidFilterException;
import com.modsensoftware.marketplace.unit.AbstractDaoTest;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
public class UserDaoTest extends AbstractDaoTest {

    @Autowired
    private UserDao underTest;

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
    public void canGetUsersWithPagination() {
        // given
        Long companyId = 1L;
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize + 1; i++) {
            User user = generateUserWithRandomEmailAndCompanyId(random, companyId);
            users.add(user);
            underTest.save(user);
        }

        // when
        List<User> firstPage = underTest.getAll(0, Collections.emptyMap());
        List<User> secondPage = underTest.getAll(1, Collections.emptyMap());

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize);
        Assertions.assertThat(secondPage.size()).isEqualTo(users.size() - pageSize);

        // clean up
        deleteAllUsers(users);
    }

    @Test
    public void canGetAllUsersFilteredByCompanyId() {
        // given
        Long companyId = 1L;
        Random random = new Random();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < pageSize / 2; i++) {
            User user = generateUserWithRandomEmailAndCompanyId(random, companyId);
            User anotherUser = generateUserWithRandomEmailAndCompanyId(random, companyId + 1);
            users.add(user);
            users.add(anotherUser);
            underTest.save(user);
            underTest.save(anotherUser);
        }

        // when
        Map<String, String> companyIdFilter = new HashMap<>();
        String filterValue = String.valueOf(companyId);
        companyIdFilter.put("companyId", filterValue);
        List<User> firstPage = underTest.getAll(0, companyIdFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize / 2);

        // clean up
        deleteAllUsers(users);
    }

    @Test
    public void canGetAllUsersFilteredByTimestampCreated() {
        // given
        Long companyId = 1L;
        List<User> users = generatePersistentUsersForTimestampFilterTest(companyId);
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
        Long companyId = 1L;
        Long anotherCompanyId = 2L;
        User user = new User(UUID.randomUUID(), "username", "email@email.com", "full name",
                now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), companyId);
        underTest.save(user);
        User updatedFields = new User(null, "upd username", "updEmail@email.com",
                "upd full name", null, now().truncatedTo(SECONDS), anotherCompanyId);
        User expected = new User(user.getId(), updatedFields.getUsername(), updatedFields.getEmail(),
                updatedFields.getName(), user.getCreated(), updatedFields.getUpdated(), updatedFields.getCompanyId());

        // when
        underTest.update(user.getId(), updatedFields);

        // then
        User actual = underTest.get(user.getId());
        Assertions.assertThat(actual).isEqualTo(expected);

        // clean up
        deleteUser(actual);
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        User user = generateDefaultTestUser();
        underTest.save(user);
        User updatedFields = new User();

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
    }

    private User generateDefaultTestUser() {
        return new User(UUID.randomUUID(), "username", "email@email.com", "full name",
                now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), 1L);
    }

    private void deleteUser(User user) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(user);
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

    private List<User> generatePersistentUsersForTimestampFilterTest(Long companyId) {
        Random random = new Random();
        User user1 = generateUserWithRandomEmailAndTimestamp(random, companyId, "2022-10-01T12:00:00");
        User user2 = generateUserWithRandomEmailAndTimestamp(random, companyId, "2022-11-01T12:00:00");
        User user3 = generateUserWithRandomEmailAndTimestamp(random, companyId, "2022-11-07T12:00:00");
        User user4 = generateUserWithRandomEmailAndTimestamp(random, companyId, "2022-11-18T12:00:00");
        List<User> users = List.of(user1, user2, user3, user4);
        Session saveSession = sessionFactory.openSession();
        Transaction saveTransaction = saveSession.beginTransaction();
        users.forEach(saveSession::save);
        saveTransaction.commit();
        saveSession.close();
        return users;
    }

    private User generateUserWithRandomEmailAndCompanyId(Random random, Long companyId) {
        return new User(UUID.randomUUID(), format("username%s", random.nextInt()),
                format("email%s@email.com", random.nextInt()), "full name",
                now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), companyId);
    }

    private User generateUserWithRandomEmailAndTimestamp(Random random, Long companyId, String timestamp) {
        return new User(UUID.randomUUID(), format("username%s", random.nextInt()),
                format("email%s@email.com", random.nextInt()), "full name",
                parse(timestamp), parse(timestamp), companyId);
    }
}

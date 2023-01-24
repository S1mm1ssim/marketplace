package com.modsensoftware.marketplace.unit.usertransaction;

import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.domain.Order;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.domain.UserTransactionStatus;
import com.modsensoftware.marketplace.dto.response.CompanyResponse;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import com.modsensoftware.marketplace.unit.AbstractDaoTest;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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
    private UserTransactionDao underTest;

    @Value("${default.page.size}")
    private int pageSize;

    @Test
    public void canSaveUserTransaction() {
        // given
        UUID userId = UUID.randomUUID();
        String positionId = "1";
        UserTransaction userTransaction = generateUserTransaction(userId, positionId);

        // when
        underTest.save(userTransaction);

        // then
        Assertions.assertThat(userTransaction.getId()).isNotNull();
        userTransaction.getOrderLine().forEach(order -> {
            Assertions.assertThat(order.getId()).isNotNull();
            Assertions.assertThat(order.getUserTransaction()).isNotNull();
            Assertions.assertThat(order.getPositionId()).isNotNull();
        });

        // clean up
        deleteUserTransaction(userTransaction);
    }

    @Test
    public void canGetAllTransactionsForUserWithPagination() {
        // given
        UserResponse user1 = generateUser("customer1@email.com", "username3", CompanyResponse.builder().id(1L).build());
        UserResponse user2 = generateUser("customer2@email.com", "username4", CompanyResponse.builder().id(2L).build());

        String positionId = "1";
        List<UserTransaction> userTransactions = generateTransactionsForUsers(positionId, user1, user2);

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
    }

    private UserResponse generateUser(String email, String username, CompanyResponse company) {
        return new UserResponse(UUID.randomUUID(), username, email, "full name",
                now().truncatedTo(SECONDS), now().truncatedTo(SECONDS), company);
    }

    private UserTransaction generateUserTransaction(UUID userId, String positionId) {
        return new UserTransaction(null, userId, now().truncatedTo(SECONDS), UserTransactionStatus.IN_PROGRESS,
                List.of(new Order(null, 5d, positionId, null))
        );
    }

    private void deleteUserTransaction(UserTransaction userTransaction) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        userTransaction.getOrderLine().forEach(session::delete);
        session.delete(userTransaction);
        transaction.commit();
        session.close();
    }

    private List<UserTransaction> generateTransactionsForUsers(String positionId, UserResponse user1, UserResponse user2) {
        List<UserTransaction> userTransactions = new ArrayList<>();
        // Generating pageSize + 1 userTransactions for each user
        for (int i = 0; i < pageSize + 1; i++) {
            UserTransaction userTransaction1 = generateUserTransaction(user1.getId(), positionId);
            UserTransaction userTransaction2 = generateUserTransaction(user2.getId(), positionId);
            userTransactions.add(userTransaction1);
            userTransactions.add(userTransaction2);
            underTest.save(userTransaction1);
            underTest.save(userTransaction2);
        }
        return userTransactions;
    }
}

package com.modsensoftware.marketplace.integration.transaction;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static com.modsensoftware.marketplace.domain.UserTransactionStatus.IN_PROGRESS;
import static com.modsensoftware.marketplace.domain.UserTransactionStatus.SUCCESS;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
@ActiveProfiles("integration-test")
@EmbeddedKafka(topics = {"userTransactionStatusResultsTest", "userTransactionProcessingTest"})
public class TransactionStatusKafkaConsumerTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, PlacedUserTransaction> kafkaTemplate;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void shouldUpdateTransactionStatusOnMessageConsumed() throws InterruptedException {
        // given
        UserTransaction transaction = new UserTransaction(null, UUID.randomUUID(),
                LocalDateTime.now(), IN_PROGRESS, new ArrayList<>());
        Session saveTransactionSession = sessionFactory.openSession();
        saveTransactionSession.beginTransaction();
        Long id = (Long) saveTransactionSession.save(transaction);
        saveTransactionSession.getTransaction().commit();
        saveTransactionSession.close();
        PlacedUserTransaction placedTransaction = new PlacedUserTransaction(transaction.getId(),
                SUCCESS, new ArrayList<>());
        // when
        kafkaTemplate.send("userTransactionStatusResultsTest", placedTransaction);
        Thread.sleep(1000);

        // then
        Session session = sessionFactory.openSession();
        UserTransaction result = session.find(UserTransaction.class, id);
        Assertions.assertThat(result.getStatus()).isEqualTo(placedTransaction.getStatus());
        session.delete(result);
        session.close();
    }
}

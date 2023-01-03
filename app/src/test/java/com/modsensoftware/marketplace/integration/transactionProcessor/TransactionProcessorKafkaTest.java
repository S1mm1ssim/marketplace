package com.modsensoftware.marketplace.integration.transactionProcessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.UserTransactionStatus;
import com.modsensoftware.marketplace.dto.OrderRequestDto;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.ext.ScriptUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.modsensoftware.marketplace.domain.UserTransactionStatus.IN_PROGRESS;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
@ActiveProfiles("integration-test")
@EmbeddedKafka(topics = {"userTransactionStatusResultsTest", "userTransactionProcessingTest"})
public class TransactionProcessorKafkaTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, PlacedUserTransaction> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private PositionDao positionDao;

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        ScriptUtils.runInitScript(dbDelegate, "integration/transactionProcessor/processorIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/transactionProcessor/processorIntegrationTestTearDown.sql");
    }

    @Test
    public void shouldProcessUserTransactionAndProduceMessage() throws JsonProcessingException {
        // given
        Long positionId = 999L;
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto(positionId, new BigDecimal("10")));
        PlacedUserTransaction placedTransaction = new PlacedUserTransaction(1L, IN_PROGRESS, orders);
        double expectedPositionAmount = 140d;

        // when
        kafkaTemplate.send("userTransactionProcessingTest", placedTransaction);

        // then
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("resultsGroup", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, String> consumer = cf.createConsumer();
        this.embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "userTransactionStatusResultsTest");
        ConsumerRecords<String, String> replies = KafkaTestUtils.getRecords(consumer);
        // Validate amount of messages sent to topic
        Assertions.assertThat(replies.count()).isEqualTo(1);

        // Validate that message value
        PlacedUserTransaction processedTransaction = objectMapper.readValue(replies.iterator().next().value(),
                PlacedUserTransaction.class);
        Assertions.assertThat(processedTransaction.getStatus()).isEqualTo(UserTransactionStatus.SUCCESS);
        Position position = positionDao.get(positionId);
        Assertions.assertThat(position.getAmount()).isEqualTo(expectedPositionAmount);
    }
}

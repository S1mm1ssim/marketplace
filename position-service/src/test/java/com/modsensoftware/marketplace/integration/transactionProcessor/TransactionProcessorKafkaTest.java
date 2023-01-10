package com.modsensoftware.marketplace.integration.transactionProcessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.OrderRequestDto;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.dto.UserTransactionStatus;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.modsensoftware.marketplace.dto.UserTransactionStatus.IN_PROGRESS;
import static java.time.LocalDateTime.now;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
@EmbeddedKafka(topics = {"userTransactionStatusResultsTest", "userTransactionProcessingTest"})
public class TransactionProcessorKafkaTest extends AbstractIntegrationTest {

    @Autowired
    private ReactiveKafkaProducerTemplate<String, PlacedUserTransaction> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private PositionDao positionDao;

    private static Position setupPosition;

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        Category parent = Category.builder().id("999").name("root")
                .description("description").build();
        Category category = Category.builder().id("1000").name("electronics").parent(parent)
                .description("electronics").build();
        Item item = Item.builder().id("12345").name("item1").description("description")
                .created(now()).category(category).build();
        setupPosition = positionDao.save(Position.builder()
                .item(item)
                .companyId(1000L)
                .createdBy("722cd920-e127-4cc2-93b9-e9b4a8f18873")
                .created(now())
                .amount(150d)
                .minAmount(0.1d)
                .build()).block();
    }

    @AfterEach
    void tearDown() {
        positionDao.deleteById(setupPosition.getId()).block();
    }

    @Test
    public void shouldProcessUserTransactionAndProduceMessage() throws JsonProcessingException {
        // given
        String positionId = setupPosition.getId();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto(positionId, new BigDecimal("10")));
        PlacedUserTransaction placedTransaction = new PlacedUserTransaction(1L, IN_PROGRESS, orders);
        double expectedPositionAmount = 140d;

        // when
        kafkaTemplate.send("userTransactionProcessingTest", placedTransaction).block();

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
        positionDao.get(positionId).as(StepVerifier::create)
                .expectNextMatches(position -> position.getAmount().equals(expectedPositionAmount))
                .expectComplete().verify();
    }
}

package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.service.TransactionStatusKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 12/28/2022
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionStatusKafkaProducerImpl implements TransactionStatusKafkaProducer {

    private final ReactiveKafkaProducerTemplate<String, PlacedUserTransaction> kafkaTemplate;

    @Value("${topics.processedTransactions.name}")
    private String processedTransactionsTopicName;

    @Override
    public Mono<Void> publishUserTransactionStatus(PlacedUserTransaction placedUserTransaction) {
        log.info("Publishing the result of user transaction processing: {}", placedUserTransaction);
        return kafkaTemplate.send(processedTransactionsTopicName, placedUserTransaction).then();
    }
}

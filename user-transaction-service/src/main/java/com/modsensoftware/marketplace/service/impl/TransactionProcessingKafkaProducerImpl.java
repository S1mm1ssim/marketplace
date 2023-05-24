package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.service.TransactionProcessingKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @author andrey.demyanchik on 12/28/2022
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionProcessingKafkaProducerImpl implements TransactionProcessingKafkaProducer {

    private final KafkaTemplate<String, PlacedUserTransaction> kafkaTemplate;

    @Value("${topics.transactionsPlacedForProcessing.name}")
    private String processingTopicName;

    @Override
    public void publishUserTransactionProcessing(PlacedUserTransaction placedUserTransaction) {
        log.info("Publishing user transaction for processing: {}", placedUserTransaction);
        ListenableFuture<SendResult<String, PlacedUserTransaction>> future
                = kafkaTemplate.send(processingTopicName, placedUserTransaction);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Failed to send transaction", ex);
            }

            @Override
            public void onSuccess(SendResult<String, PlacedUserTransaction> result) {
                log.info("Sent transaction: {} with offset: {}",
                        placedUserTransaction, result.getRecordMetadata().offset());
            }
        });
    }
}

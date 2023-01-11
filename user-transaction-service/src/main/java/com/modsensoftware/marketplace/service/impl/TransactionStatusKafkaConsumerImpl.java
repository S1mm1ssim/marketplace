package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.service.TransactionsStatusKafkaConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author andrey.demyanchik on 12/28/2022
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionStatusKafkaConsumerImpl implements TransactionsStatusKafkaConsumer {

    private final UserTransactionDao transactionDao;

    @KafkaListener(topics = "${topics.processedTransactions.name}", groupId = "${topics.processedTransactions.groupId}")
    @Override
    public void consumeTransactionStatus(PlacedUserTransaction placedUserTransaction) {
        log.info("Consumed the result of processing of the transaction: {}", placedUserTransaction);
        transactionDao.updateTransactionStatus(placedUserTransaction.getId(), placedUserTransaction.getStatus());
    }
}

package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.UserTransactionStatus;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.service.TransactionProcessingKafkaConsumer;
import com.modsensoftware.marketplace.service.TransactionStatusKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionProcessingKafkaConsumerImpl implements TransactionProcessingKafkaConsumer {

    private final PositionDao positionDao;
    private final TransactionStatusKafkaProducer producer;

    @KafkaListener(topics = "${topics.transactionsPlacedForProcessing.name}",
            groupId = "${topics.transactionsPlacedForProcessing.groupId}")
    @Override
    public void consumeTransactionProcessing(PlacedUserTransaction placedUserTransaction) {
        log.info("Consumed user transaction for processing: {}", placedUserTransaction);
        int validatedOrdersAmount = (int) placedUserTransaction.getOrderLine().stream().filter(order -> {
            Position requestedPosition = positionDao.get(order.getPositionId());
            double orderAmount = order.getAmount().doubleValue();
            return requestedPosition.getAmount() > orderAmount || requestedPosition.getMinAmount() < orderAmount;
        }).count();
        if (validatedOrdersAmount == placedUserTransaction.getOrderLine().size()) {
            placedUserTransaction.getOrderLine().forEach(order -> {
                Position requestedPosition = positionDao.get(order.getPositionId());
                double orderAmount = order.getAmount().doubleValue();
                requestedPosition.setAmount(requestedPosition.getAmount() - orderAmount);
                positionDao.update(requestedPosition.getId(), requestedPosition);
            });
            placedUserTransaction.setStatus(UserTransactionStatus.SUCCESS);
        } else {
            placedUserTransaction.setStatus(UserTransactionStatus.REJECTED);
        }
        producer.publishUserTransactionStatus(placedUserTransaction);
    }
}

package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import com.modsensoftware.marketplace.dto.UserTransactionStatus;
import com.modsensoftware.marketplace.service.TransactionProcessingKafkaConsumer;
import com.modsensoftware.marketplace.service.TransactionStatusKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Boolean> validationResults = placedUserTransaction.getOrderLine().stream()
                .map(order -> positionDao.get(order.getPositionId())
                        .onErrorStop()
                        .map(position -> {
                            double orderAmount = order.getAmount().doubleValue();
                            return position.getAmount() > orderAmount || position.getMinAmount() < orderAmount;
                        })).map(Mono::block)
                .collect(Collectors.toList());
        long invalidOrdersAmount = validationResults.stream().filter(Boolean.FALSE::equals).count();
        if (validationResults.size() < placedUserTransaction.getOrderLine().size() || invalidOrdersAmount > 0) {
            log.error("Found orders that did not pass validation. Transaction is rejected. Publishing to results topic.");
            placedUserTransaction.setStatus(UserTransactionStatus.REJECTED);
            producer.publishUserTransactionStatus(placedUserTransaction).subscribe();
        } else {
            log.info("Validation passed successfully. Decreasing positions' amounts.");
            Flux.fromIterable(placedUserTransaction.getOrderLine())
                    .subscribe(orderRequestDto -> {
                        positionDao.get(orderRequestDto.getPositionId()).subscribe(position -> {
                            double orderAmount = orderRequestDto.getAmount().doubleValue();
                            position.setAmount(position.getAmount() - orderAmount);
                            positionDao.update(position.getId(), position).subscribe();
                        });
                        log.info("Transaction processed successfully. Publishing to results topic.");
                        placedUserTransaction.setStatus(UserTransactionStatus.SUCCESS);
                        producer.publishUserTransactionStatus(placedUserTransaction).subscribe();
                    });
        }
    }
}

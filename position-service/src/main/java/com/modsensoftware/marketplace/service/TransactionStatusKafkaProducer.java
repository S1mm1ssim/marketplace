package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.PlacedUserTransaction;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 12/28/2022
 */
public interface TransactionStatusKafkaProducer {

    Mono<Void> publishUserTransactionStatus(PlacedUserTransaction placedUserTransaction);
}

package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.PlacedUserTransaction;

/**
 * @author andrey.demyanchik on 12/28/2022
 */
public interface TransactionProcessingKafkaProducer {

    void publishUserTransactionProcessing(PlacedUserTransaction placedUserTransaction);
}

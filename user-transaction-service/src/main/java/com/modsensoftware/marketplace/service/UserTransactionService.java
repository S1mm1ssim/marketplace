package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.request.UserTransactionRequestDto;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
public interface UserTransactionService {

    void createUserTransaction(UserTransactionRequestDto transactionDto);

    List<UserTransaction> getAllTransactionsForUser(String userId, int pageNumber);
}

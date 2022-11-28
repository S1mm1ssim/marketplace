package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.UserTransactionDto;
import com.modsensoftware.marketplace.service.UserTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class TransactionController {

    private final UserTransactionService transactionService;
    private static final String DEFAULT_PAGE_NUMBER = "0";

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/transactions")
    public void createUserTransaction(@Valid @RequestBody UserTransactionDto transactionDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new transaction from dto: {}", transactionDto);
        }
        transactionService.createUserTransaction(transactionDto);
    }

    @GetMapping("/{userId}/transactions")
    public List<UserTransaction> getAllTransactionsForUser(
            @PathVariable(name = "userId") UUID userId,
            @RequestParam(name = "page", defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber
    ) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all transactions for page {} for user with id: {}", pageNumber, userId);
        }
        return transactionService.getAllTransactionsForUser(userId.toString(), pageNumber);
    }
}

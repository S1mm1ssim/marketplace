package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.UserTransactionDto;
import com.modsensoftware.marketplace.service.UserTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.PAGE_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.USER_ID_PATH_VARIABLE_NAME;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class TransactionController {

    private final UserTransactionService transactionService;

    @PreAuthorize("hasAuthority('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/transactions")
    public void createUserTransaction(@Valid @RequestBody UserTransactionDto transactionDto) {
        log.debug("Creating new transaction from dto: {}", transactionDto);
        transactionService.createUserTransaction(transactionDto);
    }

    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/{userId}/transactions")
    public List<UserTransaction> getAllTransactionsForUser(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @PathVariable(name = USER_ID_PATH_VARIABLE_NAME) UUID userId
    ) {
        log.debug("Fetching all transactions for page {} for user with id: {}", pageNumber, userId);
        return transactionService.getAllTransactionsForUser(userId.toString(), pageNumber);
    }
}

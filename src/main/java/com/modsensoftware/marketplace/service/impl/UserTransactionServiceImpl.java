package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.UserTransactionDto;
import com.modsensoftware.marketplace.dto.mapper.UserTransactionMapper;
import com.modsensoftware.marketplace.service.OrderService;
import com.modsensoftware.marketplace.service.UserTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTransactionServiceImpl implements UserTransactionService {

    private final UserDao userDao;
    private final UserTransactionDao transactionDao;
    private final UserTransactionMapper transactionMapper;
    private final OrderService orderService;

    @Override
    public void createUserTransaction(UserTransactionDto transactionDto) {
        // If user doesn't exist EntityNotFoundException will be thrown
        // and caught by ExceptionHandler
        userDao.get(transactionDto.getUserId());

        if (log.isDebugEnabled()) {
            log.debug("Creating new transaction from dto: {}", transactionDto);
        }
        // If validation fails, exception will be thrown and caught by ExceptionHandler
        orderService.validateOrders(transactionDto.getOrderLine());

        UserTransaction userTransaction = transactionMapper.toUserTransaction(transactionDto);
        userTransaction.setCreated(LocalDateTime.now());
        if (log.isDebugEnabled()) {
            log.debug("Mapping result: {}", userTransaction);
        }
        transactionDao.save(userTransaction);
    }

    @Override
    public List<UserTransaction> getAllTransactionsForUser(String userId, int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all transactions for page {} for user with id: {}", pageNumber, userId);
        }
        Map<String, String> filterProps = new HashMap<>();
        filterProps.put("userId", userId);
        return transactionDao.getAll(pageNumber, filterProps);
    }
}

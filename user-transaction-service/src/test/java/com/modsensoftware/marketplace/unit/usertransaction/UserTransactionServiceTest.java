package com.modsensoftware.marketplace.unit.usertransaction;

import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.domain.UserTransactionStatus;
import com.modsensoftware.marketplace.dto.mapper.UserTransactionMapper;
import com.modsensoftware.marketplace.dto.request.OrderRequest;
import com.modsensoftware.marketplace.dto.request.UserTransactionRequest;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import com.modsensoftware.marketplace.service.OrderService;
import com.modsensoftware.marketplace.service.TransactionProcessingKafkaProducer;
import com.modsensoftware.marketplace.service.UserTransactionService;
import com.modsensoftware.marketplace.service.impl.UserClient;
import com.modsensoftware.marketplace.service.impl.UserTransactionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@ExtendWith(MockitoExtension.class)
public class UserTransactionServiceTest {

    @Mock
    private UserClient userClient;
    @Mock
    private UserTransactionDao transactionDao;
    @Mock
    private OrderService orderService;
    @Mock
    private TransactionProcessingKafkaProducer producer;

    private final UserTransactionMapper transactionMapper
            = Mappers.getMapper(UserTransactionMapper.class);

    private UserTransactionService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserTransactionServiceImpl(userClient, transactionDao, orderService, producer);
    }

    @Test
    public void canGetAllTransactionsForUser() {
        // given
        String userId = UUID.randomUUID().toString();
        int pageNumber = 0;
        Map<String, String> filterProps = new HashMap<>();
        filterProps.put("userId", userId);
        // when
        underTest.getAllTransactionsForUser(userId, pageNumber);
        // then
        BDDMockito.verify(transactionDao).getAll(pageNumber, filterProps);

    }

    @Test
    public void canSaveUserTransaction() {
        // given
        UUID userId = UUID.randomUUID();
        OrderRequest orderDto = new OrderRequest("1", new BigDecimal("5"));
        UserTransactionRequest transactionDto = new UserTransactionRequest(userId, List.of(orderDto));
        BDDMockito.when(userClient.getUserById(userId)).thenReturn(new UserResponse());
        UserTransaction expectedTransaction = transactionMapper.toUserTransaction(transactionDto);
        expectedTransaction.setStatus(UserTransactionStatus.IN_PROGRESS);

        // when
        underTest.createUserTransaction(transactionDto);

        // then
        BDDMockito.verify(orderService).validateOrders(transactionDto.getOrderLine());
        ArgumentCaptor<UserTransaction> transactionCaptor = ArgumentCaptor.forClass(UserTransaction.class);
        BDDMockito.verify(transactionDao).save(transactionCaptor.capture());

        UserTransaction transactionBeingSaved = transactionCaptor.getValue();
        transactionBeingSaved.setCreated(null);
        Assertions.assertThat(transactionBeingSaved).isEqualTo(expectedTransaction);
        BDDMockito.verify(producer).publishUserTransactionProcessing(
                transactionMapper.toPlacedUserTransaction(expectedTransaction)
        );
    }
}

package com.modsensoftware.marketplace.unit.usertransaction;

import com.modsensoftware.marketplace.dao.UserTransactionDao;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.dto.request.OrderRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import com.modsensoftware.marketplace.dto.request.UserTransactionRequestDto;
import com.modsensoftware.marketplace.dto.mapper.UserTransactionMapper;
import com.modsensoftware.marketplace.service.OrderService;
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

    private final UserTransactionMapper transactionMapper
            = Mappers.getMapper(UserTransactionMapper.class);

    private UserTransactionService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserTransactionServiceImpl(userClient, transactionDao, orderService);
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
        OrderRequestDto orderDto = new OrderRequestDto(1L, new BigDecimal("5"), 0L);
        UserTransactionRequestDto transactionDto = new UserTransactionRequestDto(userId, List.of(orderDto));
        BDDMockito.when(userClient.getUserById(userId)).thenReturn(new UserResponseDto());
        UserTransaction expectedTransaction = transactionMapper.toUserTransaction(transactionDto);

        // when
        underTest.createUserTransaction(transactionDto);

        // then
        BDDMockito.verify(orderService).validateOrders(transactionDto.getOrderLine());
        ArgumentCaptor<UserTransaction> transactionCaptor = ArgumentCaptor.forClass(UserTransaction.class);
        BDDMockito.verify(transactionDao).save(transactionCaptor.capture());
        UserTransaction transactionBeingSaved = transactionCaptor.getValue();
        transactionBeingSaved.setCreated(null);
        Assertions.assertThat(transactionBeingSaved).isEqualTo(expectedTransaction);
    }
}

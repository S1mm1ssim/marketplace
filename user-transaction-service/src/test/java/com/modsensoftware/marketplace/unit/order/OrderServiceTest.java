package com.modsensoftware.marketplace.unit.order;

import com.modsensoftware.marketplace.dto.request.OrderRequest;
import com.modsensoftware.marketplace.dto.response.CompanyResponse;
import com.modsensoftware.marketplace.dto.response.ItemResponse;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import com.modsensoftware.marketplace.exception.InsufficientItemsInStockException;
import com.modsensoftware.marketplace.exception.InsufficientOrderAmountException;
import com.modsensoftware.marketplace.service.OrderService;
import com.modsensoftware.marketplace.service.impl.OrderServiceImpl;
import com.modsensoftware.marketplace.service.impl.PositionClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private PositionClient positionClient;

    private OrderService underTest;

    private static final String INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE
            = "Not enough items in stock for position with id=%s. Wanted amount=%s. Currently in stock=%s";
    private static final String INSUFFICIENT_ORDER_AMOUNT_MESSAGE
            = "Wanted amount=%s is less than position's(id=%s) minimum amount=%s";

    @BeforeEach
    void setUp() {
        underTest = new OrderServiceImpl(positionClient);
        ReflectionTestUtils.setField(underTest, "insufficientItemsInStockMessage", INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE);
        ReflectionTestUtils.setField(underTest, "insufficientOrderAmountMessage", INSUFFICIENT_ORDER_AMOUNT_MESSAGE);
    }

    @Test
    public void shouldNotThrowAnyExceptionDuringOrdersValidation() {
        // given
        PositionResponse pos1 = PositionResponse.builder()
                .id(15L)
                .item(ItemResponse.builder().id(UUID.randomUUID()).version(0L).build())
                .company(CompanyResponse.builder().id(2L).build())
                .createdBy(UserResponse.builder().id(UUID.randomUUID()).build())
                .amount(30d).minAmount(1d).build();
        PositionResponse pos2 = PositionResponse.builder()
                .id(16L)
                .item(ItemResponse.builder().id(UUID.randomUUID()).version(0L).build())
                .company(CompanyResponse.builder().id(2L).build())
                .createdBy(UserResponse.builder().id(UUID.randomUUID()).build())
                .amount(30d).minAmount(1d).build();
        List<OrderRequest> orders = new ArrayList<>();
        orders.add(new OrderRequest(15L, new BigDecimal(5)));
        orders.add(new OrderRequest(16L, new BigDecimal(2)));
        BDDMockito.when(positionClient.getPositionById(15L)).thenReturn(pos1);
        BDDMockito.when(positionClient.getPositionById(16L)).thenReturn(pos2);

        // when
        // then
        Assertions.assertThatNoException().isThrownBy(() -> underTest.validateOrders(orders));
    }

    @Test
    public void shouldThrowInsufficientItemsInStockException() {
        // given
        PositionResponse pos = PositionResponse.builder()
                .id(16L).amount(3d).minAmount(1d).build();
        List<OrderRequest> orders = new ArrayList<>();
        orders.add(new OrderRequest(16L, new BigDecimal(4)));
        BDDMockito.when(positionClient.getPositionById(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientItemsInStockException.class)
                .hasMessage(format(INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE, pos.getId(),
                        orders.get(0).getAmount(), pos.getAmount()));
    }

    @Test
    public void shouldThrowInsufficientOrderAmountException() {
        // given
        PositionResponse pos = PositionResponse.builder()
                .id(16L).amount(30d).minAmount(5d).build();
        List<OrderRequest> orders = new ArrayList<>();
        orders.add(new OrderRequest(16L, new BigDecimal(4)));
        BDDMockito.when(positionClient.getPositionById(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientOrderAmountException.class)
                .hasMessage(format(INSUFFICIENT_ORDER_AMOUNT_MESSAGE, orders.get(0).getAmount(),
                        pos.getId(), pos.getMinAmount()));
    }
}

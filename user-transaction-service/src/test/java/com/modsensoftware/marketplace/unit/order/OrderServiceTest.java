package com.modsensoftware.marketplace.unit.order;

import com.modsensoftware.marketplace.dto.request.OrderRequestDto;
import com.modsensoftware.marketplace.dto.response.CompanyResponseDto;
import com.modsensoftware.marketplace.dto.response.ItemResponseDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
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
        PositionResponseDto pos1 = PositionResponseDto.builder()
                .id("15")
                .item(ItemResponseDto.builder().id("15").version(0L).build())
                .company(CompanyResponseDto.builder().id(2L).build())
                .createdBy(UserResponseDto.builder().id(UUID.randomUUID()).build())
                .amount(30d).minAmount(1d).build();
        PositionResponseDto pos2 = PositionResponseDto.builder()
                .id("16")
                .item(ItemResponseDto.builder().id("16").version(0L).build())
                .company(CompanyResponseDto.builder().id(2L).build())
                .createdBy(UserResponseDto.builder().id(UUID.randomUUID()).build())
                .amount(30d).minAmount(1d).build();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto("15", new BigDecimal(5)));
        orders.add(new OrderRequestDto("16", new BigDecimal(2)));
        BDDMockito.when(positionClient.getPositionById("15")).thenReturn(pos1);
        BDDMockito.when(positionClient.getPositionById("16")).thenReturn(pos2);

        // when
        // then
        Assertions.assertThatNoException().isThrownBy(() -> underTest.validateOrders(orders));
    }

    @Test
    public void shouldThrowInsufficientItemsInStockException() {
        // given
        PositionResponseDto pos = PositionResponseDto.builder()
                .id("16").amount(3d).minAmount(1d).build();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto("16", new BigDecimal(4)));
        BDDMockito.when(positionClient.getPositionById("16")).thenReturn(pos);

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
        PositionResponseDto pos = PositionResponseDto.builder()
                .id("16").amount(30d).minAmount(5d).build();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto("16", new BigDecimal(4)));
        BDDMockito.when(positionClient.getPositionById("16")).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientOrderAmountException.class)
                .hasMessage(format(INSUFFICIENT_ORDER_AMOUNT_MESSAGE, orders.get(0).getAmount(),
                        pos.getId(), pos.getMinAmount()));
    }
}

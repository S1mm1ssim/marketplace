package com.modsensoftware.marketplace.unit.order;

import com.modsensoftware.marketplace.dto.request.OrderRequestDto;
import com.modsensoftware.marketplace.dto.response.CompanyResponseDto;
import com.modsensoftware.marketplace.dto.response.ItemResponseDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.exception.InsufficientItemsInStockException;
import com.modsensoftware.marketplace.exception.InsufficientOrderAmountException;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
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
    private final PositionMapper positionMapper = new PositionMapper();

    private OrderService underTest;

    private static final String NO_POSITION_VERSION_PROVIDED_MESSAGE
            = "No version for position with id %s was provided";
    private static final String INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE
            = "Not enough items in stock for position with id=%s. Wanted amount=%s. Currently in stock=%s";
    private static final String INSUFFICIENT_ORDER_AMOUNT_MESSAGE
            = "Wanted amount=%s is less than position's(id=%s) minimum amount=%s";

    @BeforeEach
    void setUp() {
        underTest = new OrderServiceImpl(positionClient, positionMapper);
        ReflectionTestUtils.setField(underTest, "noPositionVersionProvidedMessage", NO_POSITION_VERSION_PROVIDED_MESSAGE);
        ReflectionTestUtils.setField(underTest, "insufficientItemsInStockMessage", INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE);
        ReflectionTestUtils.setField(underTest, "insufficientOrderAmountMessage", INSUFFICIENT_ORDER_AMOUNT_MESSAGE);
    }

    @Test
    public void shouldNotThrowAnyExceptionDuringOrdersValidation() {
        // given
        PositionResponseDto pos1 = PositionResponseDto.builder()
                .id(15L)
                .item(ItemResponseDto.builder().id(UUID.randomUUID()).version(0L).build())
                .company(CompanyResponseDto.builder().id(2L).build())
                .createdBy(UserResponseDto.builder().id(UUID.randomUUID()).build())
                .amount(30d).minAmount(1d).version(0L).build();
        PositionResponseDto pos2 = PositionResponseDto.builder()
                .id(16L)
                .item(ItemResponseDto.builder().id(UUID.randomUUID()).version(0L).build())
                .company(CompanyResponseDto.builder().id(2L).build())
                .createdBy(UserResponseDto.builder().id(UUID.randomUUID()).build())
                .amount(30d).minAmount(1d).version(0L).build();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto(15L, new BigDecimal(5), 0L));
        orders.add(new OrderRequestDto(16L, new BigDecimal(2), 0L));
        BDDMockito.when(positionClient.getPositionById(15L)).thenReturn(pos1);
        BDDMockito.when(positionClient.getPositionById(16L)).thenReturn(pos2);

        // when
        // then
        Assertions.assertThatNoException().isThrownBy(() -> underTest.validateOrders(orders));
        BDDMockito.verify(positionClient).updatePosition(pos1.getId(), positionMapper.toPositionRequestDto(pos1));
        BDDMockito.verify(positionClient).updatePosition(pos2.getId(), positionMapper.toPositionRequestDto(pos2));
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionIfNoPositionVersionProvided() {
        // given
        Long positionId = 16L;
        List<OrderRequestDto> orders = new ArrayList<>();
        // Null version provided
        orders.add(new OrderRequestDto(positionId, new BigDecimal(4), null));

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(NoVersionProvidedException.class)
                .hasMessage(format(NO_POSITION_VERSION_PROVIDED_MESSAGE, positionId));
        BDDMockito.verify(positionClient, BDDMockito.never()).getPositionById(BDDMockito.any());
        BDDMockito.verify(positionClient, BDDMockito.never()).updatePosition(BDDMockito.any(), BDDMockito.any());
    }

    @Test
    public void shouldThrowInsufficientItemsInStockException() {
        // given
        PositionResponseDto pos = PositionResponseDto.builder()
                .id(16L).amount(3d).minAmount(1d).version(0L).build();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto(16L, new BigDecimal(4), 0L));
        BDDMockito.when(positionClient.getPositionById(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientItemsInStockException.class)
                .hasMessage(format(INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE, pos.getId(),
                        orders.get(0).getAmount(), pos.getAmount()));
        BDDMockito.verify(positionClient, BDDMockito.never()).updatePosition(BDDMockito.any(), BDDMockito.any());
    }

    @Test
    public void shouldThrowInsufficientOrderAmountException() {
        // given
        PositionResponseDto pos = PositionResponseDto.builder()
                .id(16L).amount(30d).minAmount(5d).version(0L).build();
        List<OrderRequestDto> orders = new ArrayList<>();
        orders.add(new OrderRequestDto(16L, new BigDecimal(4), 0L));
        BDDMockito.when(positionClient.getPositionById(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientOrderAmountException.class)
                .hasMessage(format(INSUFFICIENT_ORDER_AMOUNT_MESSAGE, orders.get(0).getAmount(),
                        pos.getId(), pos.getMinAmount()));
        BDDMockito.verify(positionClient, BDDMockito.never()).updatePosition(BDDMockito.any(), BDDMockito.any());
    }
}

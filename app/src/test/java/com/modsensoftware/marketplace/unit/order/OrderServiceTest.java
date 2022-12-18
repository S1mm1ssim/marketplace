package com.modsensoftware.marketplace.unit.order;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.OrderDto;
import com.modsensoftware.marketplace.exception.InsufficientItemsInStockException;
import com.modsensoftware.marketplace.exception.InsufficientOrderAmountException;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.service.OrderService;
import com.modsensoftware.marketplace.service.impl.OrderServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Mock
    private PositionDao positionDao;

    private OrderService underTest;

    private static final String NO_POSITION_VERSION_PROVIDED_MESSAGE
            = "No version for position with id %s was provided";
    private static final String INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE
            = "Not enough items in stock for position with id=%s. Wanted amount=%s. Currently in stock=%s";
    private static final String INSUFFICIENT_ORDER_AMOUNT_MESSAGE
            = "Wanted amount=%s is less than position's(id=%s) minimum amount=%s";

    @BeforeEach
    void setUp() {
        underTest = new OrderServiceImpl(positionDao);
        ReflectionTestUtils.setField(underTest, "noPositionVersionProvidedMessage", NO_POSITION_VERSION_PROVIDED_MESSAGE);
        ReflectionTestUtils.setField(underTest, "insufficientItemsInStockMessage", INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE);
        ReflectionTestUtils.setField(underTest, "insufficientOrderAmountMessage", INSUFFICIENT_ORDER_AMOUNT_MESSAGE);
    }

    @Test
    public void shouldNotThrowAnyExceptionDuringOrdersValidation() {
        // given
        Position pos1 = new Position(15L, null, null, null, null, 30d, 1d, 0L);
        Position pos2 = new Position(16L, null, null, null, null, 30d, 1d, 0L);
        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto(15L, new BigDecimal(5), 0L));
        orders.add(new OrderDto(16L, new BigDecimal(2), 0L));
        BDDMockito.when(positionDao.get(15L)).thenReturn(pos1);
        BDDMockito.when(positionDao.get(16L)).thenReturn(pos2);

        // when
        // then
        Assertions.assertThatNoException().isThrownBy(() -> underTest.validateOrders(orders));
        BDDMockito.verify(positionDao).update(pos1.getId(), pos1);
        BDDMockito.verify(positionDao).update(pos2.getId(), pos2);
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionIfNoPositionVersionProvided() {
        // given
        Long positionId = 16L;
        List<OrderDto> orders = new ArrayList<>();
        // Null version provided
        orders.add(new OrderDto(positionId, new BigDecimal(4), null));

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(NoVersionProvidedException.class)
                .hasMessage(format(NO_POSITION_VERSION_PROVIDED_MESSAGE, positionId));
        BDDMockito.verify(positionDao, BDDMockito.never()).get(BDDMockito.any());
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }

    @Test
    public void shouldThrowInsufficientItemsInStockException() {
        // given
        Position pos = new Position(16L, null, null, null, null, 3d, 1d, 0L);
        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto(16L, new BigDecimal(4), 0L));
        BDDMockito.when(positionDao.get(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientItemsInStockException.class)
                .hasMessage(format(INSUFFICIENT_ITEMS_IN_STOCK_MESSAGE, pos.getId(),
                        orders.get(0).getAmount(), pos.getAmount()));
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }

    @Test
    public void shouldThrowInsufficientOrderAmountException() {
        // given
        Position pos = new Position(16L, null, null, null, null, 30d, 5d, 0L);
        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto(16L, new BigDecimal(4), 0L));
        BDDMockito.when(positionDao.get(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientOrderAmountException.class)
                .hasMessage(format(INSUFFICIENT_ORDER_AMOUNT_MESSAGE, orders.get(0).getAmount(),
                        pos.getId(), pos.getMinAmount()));
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }
}

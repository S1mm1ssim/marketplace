package com.modsensoftware.marketplace.unit.order;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.OrderDto;
import com.modsensoftware.marketplace.exception.InsufficientItemsInStockException;
import com.modsensoftware.marketplace.exception.InsufficientOrderAmountException;
import com.modsensoftware.marketplace.service.OrderService;
import com.modsensoftware.marketplace.service.impl.OrderServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private PositionDao positionDao;

    private OrderService underTest;

    @BeforeEach
    void setUp() {
        underTest = new OrderServiceImpl(positionDao);
    }

    @Test
    public void shouldNotThrowAnyException() {
        // given
        Position pos1 = new Position(15L, null, null, null, null, 30d, 1d, null);
        Position pos2 = new Position(16L, null, null, null, null, 30d, 1d, null);
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
    public void shouldThrowInsufficientItemsInStockException() {
        // given
        Position pos = new Position(16L, null, null, null, null, 3d, 1d, null);
        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto(16L, new BigDecimal(4), 0L));
        BDDMockito.when(positionDao.get(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientItemsInStockException.class)
                .hasMessage(format("Not enough items in stock for position with id=%s. Wanted amount=%s. "
                                + "Currently in stock=%s", pos.getId(),
                        orders.get(0).getAmount(), pos.getAmount()));
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }

    @Test
    public void shouldThrowInsufficientOrderAmountException() {
        // given
        Position pos = new Position(16L, null, null, null, null, 30d, 5d, null);
        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto(16L, new BigDecimal(4), 0L));
        BDDMockito.when(positionDao.get(16L)).thenReturn(pos);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.validateOrders(orders))
                .isInstanceOf(InsufficientOrderAmountException.class)
                .hasMessage(format("Wanted amount=%s is less than position's(id=%s) minimum amount=%s",
                        orders.get(0).getAmount(), pos.getId(), pos.getMinAmount()));
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }
}

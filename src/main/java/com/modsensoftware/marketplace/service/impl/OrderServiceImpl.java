package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.OrderDto;
import com.modsensoftware.marketplace.exception.InsufficientItemsInStockException;
import com.modsensoftware.marketplace.exception.InsufficientOrderAmountException;
import com.modsensoftware.marketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final PositionDao positionDao;

    @Override
    public void validateOrders(Collection<OrderDto> orders) {
        if (log.isDebugEnabled()) {
            log.debug("Validating orders: {}", orders);
        }
        orders.forEach(orderDto -> {
            if (log.isDebugEnabled()) {
                log.debug("Validating order: {}", orderDto);
            }
            Position position = positionDao.get(orderDto.getPositionId());
            if (position.getAmount() < orderDto.getAmount().doubleValue()) {
                log.error("Wanted amount is bigger than position with id {} has in stock", position.getId());
                if (log.isDebugEnabled()) {
                    log.debug("Wanted amount={}. Currently in stock={}",
                            orderDto.getAmount(), position.getAmount());
                }
                throw new InsufficientItemsInStockException(
                        format("Not enough items in stock for position with id=%s. Wanted amount=%s. "
                                        + "Currently in stock=%s", position.getId(),
                                orderDto.getAmount(), position.getAmount())
                );
            }
            if (position.getMinAmount() > orderDto.getAmount().doubleValue()) {
                log.error("Wanted amount is less than position's(id={}) minimum amount", position.getId());
                if (log.isDebugEnabled()) {
                    log.debug("Wanted amount: {}. Minimum value: {}", orderDto.getAmount(), position.getMinAmount());
                }
                throw new InsufficientOrderAmountException(
                        format("Wanted amount=%s is less than position's(id=%s) minimum amount=%s",
                                orderDto.getAmount(), position.getId(), position.getMinAmount())
                );
            }
            position.setAmount(position.getAmount() - orderDto.getAmount().doubleValue());
            positionDao.update(position.getId(), position);
        });
    }
}

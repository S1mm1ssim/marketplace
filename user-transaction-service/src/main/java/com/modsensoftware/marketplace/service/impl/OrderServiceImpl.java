package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dto.request.OrderRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.modsensoftware.marketplace.exception.InsufficientItemsInStockException;
import com.modsensoftware.marketplace.exception.InsufficientOrderAmountException;
import com.modsensoftware.marketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final PositionClient positionClient;

    @Value("${exception.message.insufficientItemsInStock}")
    private String insufficientItemsInStockMessage;
    @Value("${exception.message.insufficientOrderAmount}")
    private String insufficientOrderAmountMessage;

    @Override
    public void validateOrders(Collection<OrderRequestDto> orders) {
        log.debug("Validating orders: {}", orders);
        orders.forEach(orderDto -> {
            log.debug("Validating order: {}", orderDto);
            PositionResponseDto position = positionClient.getPositionById(orderDto.getPositionId());
            if (position.getAmount() < orderDto.getAmount().doubleValue()) {
                log.error("Wanted amount is bigger than position with id {} has in stock", position.getId());
                log.debug("Wanted amount: {}. Currently in stock: {}",
                        orderDto.getAmount(), position.getAmount());
                throw new InsufficientItemsInStockException(
                        format(insufficientItemsInStockMessage, position.getId(),
                                orderDto.getAmount(), position.getAmount())
                );
            }
            if (position.getMinAmount() > orderDto.getAmount().doubleValue()) {
                log.error("Wanted amount is less than position's(id={}) minimum amount", position.getId());
                log.debug("Wanted amount: {}. Minimum value: {}", orderDto.getAmount(), position.getMinAmount());
                throw new InsufficientOrderAmountException(
                        format(insufficientOrderAmountMessage, orderDto.getAmount(),
                                position.getId(), position.getMinAmount())
                );
            }
        });
    }
}

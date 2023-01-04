package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.OrderRequest;

import java.util.Collection;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
public interface OrderService {

    void validateOrders(Collection<OrderRequest> orders);
}

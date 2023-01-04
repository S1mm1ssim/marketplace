package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Order;
import com.modsensoftware.marketplace.dto.request.OrderRequest;
import org.mapstruct.Mapper;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toOrder(OrderRequest orderRequest);
}

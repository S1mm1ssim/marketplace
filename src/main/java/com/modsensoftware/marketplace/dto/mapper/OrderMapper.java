package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Order;
import com.modsensoftware.marketplace.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mappings({
            @Mapping(target = "position.id", source = "positionId"),
            @Mapping(target = "position.version", source = "positionVersion")
    })
    Order toOrder(OrderDto orderDto);
}

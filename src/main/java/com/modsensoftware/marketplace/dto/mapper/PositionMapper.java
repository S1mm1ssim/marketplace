package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Mapper(componentModel = "spring")
public interface PositionMapper {

    @Mappings({
            @Mapping(target = "item.id", source = "positionDto.itemId"),
            @Mapping(target = "item.version", source = "itemVersion"),
            @Mapping(target = "company.id", source = "positionDto.companyId"),
            @Mapping(target = "createdBy.id", source = "positionDto.createdBy")
    })
    Position toPosition(PositionDto positionDto);
}

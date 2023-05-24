package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mappings({
            @Mapping(target = "category.id", source = "itemDto.categoryId")
    })
    Item toItem(ItemDto itemDto);
}

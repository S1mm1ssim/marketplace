package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
public interface CategoryMapper {

    /**
     * Maps <code>CategoryDto</code> object to <code>Category</code> object.
     * <code>Category</code> object may have name, description and parentId values populated.
     * In case categoryDto.nullParent is true then Category's parentId will also be null.
     * Else parentId value is checked and, if not null, set to Category.parent.id
     *
     * @param categoryDto Object of CategoryDto type populated with values according to mapping contract
     * @return Mapped Category object
     */
    Category toCategory(CategoryDto categoryDto);
}

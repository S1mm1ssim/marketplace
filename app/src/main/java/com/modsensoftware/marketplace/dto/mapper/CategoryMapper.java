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
     * If categoryDto.parentId is not null, then category.parent.id value is set.
     * If categoryDto.nullParent is true and categoryDto.parentId is null, then category.parent.id value is set to null.
     * Otherwise, category.parent is null.
     *
     * @param categoryDto Object of CategoryDto type populated with values according to mapping contract
     * @return Mapped Category object
     */
    Category toCategory(CategoryDto categoryDto);
}

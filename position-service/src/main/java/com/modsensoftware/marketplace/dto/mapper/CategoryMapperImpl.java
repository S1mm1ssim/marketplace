package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import org.springframework.stereotype.Component;

/**
 * @author andrey.demyanchik on 11/14/2022
 */
@Component
public class CategoryMapperImpl implements CategoryMapper {

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
    @Override
    public Category toCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        if (categoryDto.getParentId() == null && categoryDto.getNullParent() != null) {
            if (categoryDto.getNullParent().equals(true)) {
                category.setParent(new Category());
            }
        } else if (categoryDto.getParentId() != null) {
            Category parent = new Category();
            parent.setId(categoryDto.getParentId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        return category;
    }
}

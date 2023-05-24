package com.modsensoftware.marketplace.unit.category;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapper;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapperImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
public class CategoryMapperTest {

    private final CategoryMapper underTest = new CategoryMapperImpl();

    @ValueSource(strings = {"1"})
    @NullSource
    @ParameterizedTest
    public void shouldMapDtoToCategoryWithProvidedParentIdValue(String parentId) {
        // given
        CategoryDto categoryDto = new CategoryDto(null, null, parentId, true);

        // when
        Category actual = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        Category parent = new Category();
        parent.setId(parentId);
        expected.setParent(parent);
        Assertions.assertThat(actual.getParent()).isEqualTo(expected.getParent());
    }

    @ValueSource(booleans = {false})
    @NullSource
    @ParameterizedTest
    public void shouldMapDtoToCategoryWithNullParent(Boolean nullParent) {
        // given
        CategoryDto categoryDto = new CategoryDto(null, null, null, nullParent);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Assertions.assertThat(category.getParent()).isNull();
    }


    @Test
    public void shouldMapCategoryName() {
        // given
        String name = "name";
        CategoryDto categoryDto = new CategoryDto(name, null, null, false);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        expected.setName(name);
        Assertions.assertThat(category.getName()).isEqualTo(expected.getName());
    }

    @Test
    public void shouldMapCategoryDescription() {
        // given
        String description = "description";
        CategoryDto categoryDto = new CategoryDto(null, description, null, false);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        expected.setDescription(description);
        Assertions.assertThat(category.getName()).isEqualTo(expected.getName());
    }
}

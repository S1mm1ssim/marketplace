package com.modsensoftware.marketplace.unit.category;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapper;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapperImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
public class CompanyMapperTest {

    private final CategoryMapper underTest = new CategoryMapperImpl();

    @Test
    public void shouldMapDtoToNullParent() {
        // given
        CategoryDto categoryDto = new CategoryDto(null, null, null, true);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        expected.setParent(null);
        Assertions.assertThat(category.getParent()).isEqualTo(expected.getParent());
    }

    @Test
    public void shouldMapDtoToNullParentEvenWithParentIdProvided() {
        // given
        CategoryDto categoryDto = new CategoryDto(null, null, 1L, true);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        expected.setParent(null);
        Assertions.assertThat(category.getParent()).isEqualTo(expected.getParent());
    }

    @Test
    public void shouldMapDtoToNullParentIfParamsNotProvided() {
        // given
        CategoryDto categoryDto = new CategoryDto(null, null, null, false);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        expected.setParent(null);
        Assertions.assertThat(category.getParent()).isEqualTo(expected.getParent());
    }

    @Test
    public void shouldMapDtoToNotNullParent() {
        // given
        Long parentId = 1L;
        CategoryDto categoryDto = new CategoryDto(null, null, parentId, false);

        // when
        Category category = underTest.toCategory(categoryDto);

        // then
        Category expected = new Category();
        Category parent = new Category();
        parent.setId(parentId);
        expected.setParent(parent);
        Assertions.assertThat(category.getParent()).isEqualTo(expected.getParent());
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

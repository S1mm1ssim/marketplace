package com.modsensoftware.marketplace.unit.category;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.unit.AbstractDaoTest;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/21/2022
 */
public class CategoryDaoTest extends AbstractDaoTest {

    @Autowired
    private CategoryDao underTest;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.categoryNotFound}")
    private String categoryNotFoundMessage;

    @Test
    public void canSaveCategory() {
        // given
        String categoryName = "root";
        Category category = new Category(null, categoryName, "Root category", null);

        // when
        underTest.save(category);

        // then
        Session session = sessionFactory.openSession();
        Query<Category> query = session.createQuery("from Category where name = :root", Category.class);
        query.setParameter("root", categoryName);
        List<Category> resultList = query.getResultList();
        Assertions.assertThat(resultList).hasSize(1);

        // clean up
        underTest.deleteById(resultList.get(0).getId());
    }

    @Test
    public void canDeleteCategory() {
        // given
        Category category = new Category(null, "root", "Root category", null);

        // when
        underTest.save(category);
        underTest.deleteById(category.getId());

        // then
        Session session = sessionFactory.openSession();
        Query<Category> query = session.createQuery("from Category", Category.class);
        List<Category> resultList = query.getResultList();
        Assertions.assertThat(resultList).isEmpty();
    }

    @Test
    public void canGetAll() {
        // given
        Category category1 = new Category(null, "root", "Root category", null);
        Category category2 = new Category(null, "category", "Category with parent", category1);
        List<Category> expected = List.of(category1, category2);
        underTest.save(category1);
        underTest.save(category2);

        // when
        List<Category> actual = underTest.getAll(0, Collections.emptyMap());

        // then
        Assertions.assertThat(actual).hasSize(expected.size());
        Assertions.assertThat(actual).isEqualTo(expected);

        // clean up
        underTest.deleteById(actual.get(1).getId());
        underTest.deleteById(actual.get(0).getId());
    }

    @Test
    public void shouldReturnPageSizeAmountOfEntities() {
        // given
        List<Category> categories = new ArrayList<>();
        Category testCategory;
        for (int i = 0; i < pageSize + 1; i++) {
            testCategory = new Category(null, "category" + i, "", null);
            categories.add(testCategory);
            underTest.save(testCategory);
        }

        // when
        List<Category> firstPageElemTotal = underTest.getAll(0, Collections.emptyMap());
        List<Category> secondPageElemNumber = underTest.getAll(1, Collections.emptyMap());

        // then
        Assertions.assertThat(firstPageElemTotal).hasSize(pageSize);
        Assertions.assertThat(secondPageElemNumber).hasSize(categories.size() - pageSize);

        // clean up
        categories.forEach(category -> underTest.deleteById(category.getId()));
    }

    @Test
    public void canGetById() {
        // given
        Category category = new Category(null, "root", "Root category", null);
        underTest.save(category);

        // when
        Category actual = underTest.get(category.getId());

        // then
        Assertions.assertThat(actual).isEqualTo(category);

        // clean up
        underTest.deleteById(category.getId());
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetForNonexistentEntity() {
        // given
        Long nonexistentId = 0L;

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(nonexistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format(categoryNotFoundMessage, nonexistentId));
    }

    @Test
    public void canUpdateCategory() {
        // given
        Category parent1 = new Category(null, "parent1", "description", null);
        Category parent2 = new Category(null, "parent2", "description", null);
        Category category = new Category(null, "category", "description", parent1);
        Category updatedFields = new Category(null, "updatedCategory", "description", parent2);
        underTest.save(parent1);
        underTest.save(parent2);
        underTest.save(category);
        Category expected = new Category(category.getId(),
                updatedFields.getName(), updatedFields.getDescription(), parent2);

        // when
        underTest.update(category.getId(), updatedFields);
        Category actual = underTest.get(category.getId());

        // then
        Assertions.assertThat(actual).isEqualTo(expected);

        // clean up
        underTest.deleteById(category.getId());
        underTest.deleteById(parent1.getId());
        underTest.deleteById(parent2.getId());
    }

    @Test
    public void noMutationOnNoUpdateFieldsProvided() {
        // given
        Category parent = new Category(null, "parent", "description", null);
        Category category = new Category(null, "category", "description", parent);
        underTest.save(parent);
        underTest.save(category);

        // when
        underTest.update(category.getId(), new Category());
        Category actual = underTest.get(category.getId());

        // then
        Assertions.assertThat(actual).isEqualTo(category);

        // clean up
        underTest.deleteById(category.getId());
        underTest.deleteById(parent.getId());
    }
}

package com.modsensoftware.marketplace.unit.category;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/21/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CategoryDaoTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CategoryDao underTest;

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    private static final int PAGE_SIZE = 20;

    @BeforeEach
    void setUp() {
        underTest = new CategoryDao(sessionFactory);
        ReflectionTestUtils.setField(underTest, "pageSize", PAGE_SIZE);
    }

    @Test
    public void canSaveCategory() {
        // given
        Category category = new Category();
        category.setName("root");
        category.setDescription("Root category");
        category.setParent(null);

        // when
        underTest.save(category);

        // then
        Session session = sessionFactory.openSession();
        Query<Category> query = session.createQuery("from Category where name = :root", Category.class);
        query.setParameter("root", "root");
        List<Category> resultList = query.getResultList();
        Assertions.assertThat(resultList).hasSize(1);

        // clean up
        underTest.deleteById(resultList.get(0).getId());
    }

    @Test
    public void canDeleteCategory() {
        // given
        Category category = new Category();
        category.setName("root");
        category.setDescription("Root category");
        category.setParent(null);

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
        Category category1 = new Category();
        category1.setName("root");
        category1.setDescription("Root category");
        category1.setParent(null);

        Category category2 = new Category();
        category2.setName("category");
        category2.setDescription("Category with parent");
        category2.setParent(category1);
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
        for (int i = 0; i < PAGE_SIZE + 1; i++) {
            testCategory = new Category();
            testCategory.setName("category" + i);
            categories.add(testCategory);
            underTest.save(testCategory);
        }

        // when
        List<Category> firstPageElemTotal = underTest.getAll(0, Collections.emptyMap());
        List<Category> secondPageElemNumber = underTest.getAll(1, Collections.emptyMap());

        // then
        Assertions.assertThat(firstPageElemTotal).hasSize(PAGE_SIZE);
        Assertions.assertThat(secondPageElemNumber).hasSize(categories.size() - PAGE_SIZE);

        // clean up
        categories.forEach(category -> underTest.deleteById(category.getId()));
    }

    @Test
    public void canGetById() {
        // given
        Category category = new Category();
        category.setName("root");
        category.setDescription("Root category");
        category.setParent(null);

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
                .hasMessage(format("Category entity with id=%s is not present.", nonexistentId));
    }

    @Test
    public void canUpdateCategory() {
        // given
        Category parent1 = new Category();
        parent1.setName("parent1");

        Category parent2 = new Category();
        parent2.setName("parent2");

        Category category = new Category();
        category.setName("category");
        category.setParent(parent1);

        Category updatedFields = new Category();
        updatedFields.setName("updatedCategory");
        updatedFields.setDescription("description");
        updatedFields.setParent(parent2);

        underTest.save(parent1);
        underTest.save(parent2);
        underTest.save(category);

        Category expected = new Category(category.getId(),
                updatedFields.getName(), updatedFields.getDescription(), parent2);

        // when
        underTest.update(category.getId(), updatedFields);
        Category result = underTest.get(category.getId());

        // then
        Assertions.assertThat(result).isEqualTo(expected);

        // clean up
        underTest.deleteById(category.getId());
        underTest.deleteById(parent1.getId());
        underTest.deleteById(parent2.getId());
    }

    @Test
    public void noMutationOnNoUpdateFieldsProvided() {
        // given
        Category parent = new Category();
        parent.setName("parent");

        Category category = new Category();
        category.setName("category");
        category.setParent(parent);

        Category updatedFields = new Category();

        underTest.save(parent);
        underTest.save(category);

        // when
        underTest.update(category.getId(), updatedFields);
        Category result = underTest.get(category.getId());

        // then
        Assertions.assertThat(result).isEqualTo(category);

        // clean up
        underTest.deleteById(category.getId());
        underTest.deleteById(parent.getId());
    }
}

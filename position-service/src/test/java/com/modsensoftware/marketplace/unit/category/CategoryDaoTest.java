package com.modsensoftware.marketplace.unit.category;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.unit.AbstractDaoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author andrey.demyanchik on 11/21/2022
 */
public class CategoryDaoTest extends AbstractDaoTest {

    private CategoryDao underTest;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.categoryNotFound}")
    private String categoryNotFoundMessage;

    @BeforeEach
    void setUp() {
        underTest = new CategoryDao(mongoTemplate);
        ReflectionTestUtils.setField(underTest, "pageSize", pageSize);
        ReflectionTestUtils.setField(underTest, "categoryNotFoundMessage", categoryNotFoundMessage);
    }

    @Test
    public void canSaveCategory() {
        // given
        String categoryName = "root";
        Category category = new Category(null, categoryName, "Root category", null);

        // when
        underTest.save(category).block();

        // then
        underTest.get(category.getId())
                .as(StepVerifier::create)
                .expectNextMatches(category1 -> category1.equals(category))
                .verifyComplete();
        // clean up
        underTest.deleteById(category.getId()).block();
    }

    @Test
    public void canDeleteCategory() {
        // given
        Category category = new Category(null, "root", "Root category", null);
        underTest.save(category).block();
        // when
        underTest.deleteById(category.getId()).block();
        // then
        mongoTemplate
                .find(new Query(Criteria.where("name").is(category.getName())), Category.class)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    public void canGetAll() {
        // given
        Category category1 = new Category(null, "root", "Root category", null);
        Category category2 = new Category(null, "category", "Category with parent", category1);
        underTest.save(category1).block();
        underTest.save(category2).block();

        // when
        // then
        underTest.getAll(0, Collections.emptyMap())
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        // clean up
        underTest.getAll(0, Collections.emptyMap())
                .doOnNext(category -> underTest.deleteById(category.getId()))
                .subscribe();
    }

    @Test
    public void shouldReturnPageSizeAmountOfEntities() {
        // given
        List<Category> categories = new ArrayList<>();
        Category testCategory;
        for (int i = 0; i < pageSize + 1; i++) {
            testCategory = new Category(null, "category" + i, "", null);
            categories.add(testCategory);
            underTest.save(testCategory).block();
        }

        Flux<Category> categoryFlux = Flux.fromIterable(categories);

        categoryFlux.subscribe(saved -> {
            // when
            // then
            underTest.getAll(0, Collections.emptyMap())
                    .as(StepVerifier::create)
                    .expectNextCount(pageSize)
                    .verifyComplete();
            underTest.getAll(1, Collections.emptyMap())
                    .as(StepVerifier::create)
                    .expectNextCount(categories.size() - pageSize)
                    .verifyComplete();

            // clean up
            underTest.getAll(0, Collections.emptyMap())
                    .doOnNext(category -> underTest.deleteById(category.getId()))
                    .subscribe();
            underTest.getAll(1, Collections.emptyMap())
                    .doOnNext(category -> underTest.deleteById(category.getId()))
                    .subscribe();
        });
    }

    @Test
    public void canGetById() {
        // given
        Category category = new Category(null, "root", "Root category", null);
        underTest.save(category).block();
        // when
        //then
        underTest.get(category.getId())
                .as(StepVerifier::create)
                .expectNext(category)
                .verifyComplete();
        // clean up
        underTest.deleteById(category.getId()).block();
    }


    @Test
    public void shouldThrowEntityNotFoundExceptionWhenGetForNonexistentEntity() {
        // given
        String nonexistentId = "0454545";
        // when
        // then
        underTest.get(nonexistentId)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable.getClass().equals(EntityNotFoundException.class)
                        && throwable.getMessage().equals(String.format(categoryNotFoundMessage, nonexistentId)))
                .verify();
    }

    @Test
    public void canUpdateCategory() {
        // given
        Category parent1 = new Category(null, "parent1", "description1", null);
        Category parent2 = new Category(null, "parent2", "description2", null);
        Category category = new Category(null, "category", "description", parent1);
        underTest.save(parent1).block();
        underTest.save(parent2).block();
        underTest.save(category).block();

        Category updatedFields = new Category(null, "updCategory", "updDescr",
                Category.builder().id(parent2.getId()).build());

        // when
        underTest.update(category.getId(), updatedFields).block();

        // then
        Category expected = new Category(category.getId(), updatedFields.getName(),
                updatedFields.getDescription(), parent2);
        underTest.get(category.getId())
                .as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();

        // clean up
        underTest.deleteById(category.getId()).block();
        underTest.deleteById(parent1.getId()).block();
        underTest.deleteById(parent2.getId()).block();
    }
}

package com.modsensoftware.marketplace.unit.item;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
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

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/22/2022
 */
public class ItemDaoTest extends AbstractDaoTest {

    private ItemDao underTest;
    private CategoryDao categoryDao;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.itemNotFound}")
    private String itemNotFoundMessage;

    @BeforeEach
    void setUp() {
        categoryDao = new CategoryDao(mongoTemplate);
        underTest = new ItemDao(mongoTemplate, categoryDao);
        ReflectionTestUtils.setField(underTest, "pageSize", pageSize);
        ReflectionTestUtils.setField(underTest, "itemNotFoundMessage", itemNotFoundMessage);
    }

    @Test
    public void canSaveItem() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category).block();
        Item item = new Item(null, "name", "description", now().truncatedTo(SECONDS), category, 1L);

        // when
        underTest.save(item).block();

        // then
        underTest.get(item.getId())
                .as(StepVerifier::create)
                .expectNextMatches(item1 -> item1.equals(item))
                .verifyComplete();

        // clean up
        underTest.deleteById(item.getId()).block();
        categoryDao.deleteById(category.getId()).block();
    }

    @Test
    public void canGetItemById() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category).block();
        Item item = new Item(null, "name", "description", now().truncatedTo(SECONDS), category, 1L);
        underTest.save(item).block();

        // when
        // then
        underTest.get(item.getId())
                .as(StepVerifier::create)
                .expectNext(item)
                .verifyComplete();

        // clean up
        categoryDao.deleteById(category.getId()).block();
        underTest.deleteById(item.getId()).block();
    }

    @Test
    public void getByNonExistentIdShouldThrowEntityNotFoundException() {
        // given
        String nonExistentId = "0454545";
        // when
        // then
        underTest.get(nonExistentId)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable.getClass().equals(EntityNotFoundException.class)
                        && throwable.getMessage().equals(String.format(itemNotFoundMessage, nonExistentId)))
                .verify();
    }

    @Test
    public void canGetAllItemsWithPagination() {
        // given
        List<Item> items = new ArrayList<>();
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        for (int i = 0; i < pageSize + 1; i++) {
            Item item = new Item(null, "name", "description", now(), category, 1L);
            items.add(item);
            underTest.save(item).block();
        }
        Flux<Item> itemFlux = Flux.fromIterable(items);
        itemFlux.subscribe(saved -> {
            // when
            underTest.getAll(0, Collections.emptyMap())
                    // then
                    .as(StepVerifier::create)
                    .expectNextCount(pageSize)
                    .verifyComplete();
            underTest.getAll(1, Collections.emptyMap())
                    .as(StepVerifier::create)
                    .expectNextCount(items.size() - pageSize)
                    .verifyComplete();
        });
        // clean up
        underTest.getAll(0, Collections.emptyMap())
                .subscribe(item -> underTest.deleteById(item.getId()));
        underTest.getAll(1, Collections.emptyMap())
                .subscribe(item -> underTest.deleteById(item.getId()));
        categoryDao.deleteById(category.getId());
    }

    @Test
    public void canUpdateItem() {
        // given
        Category category = new Category(null, "another category", "other description", null);
        Category updCategory = new Category(null, "category", "description", null);
        long itemVersion = 1L;
        Item item = new Item(null, "name", "description",
                now().truncatedTo(SECONDS), category, itemVersion);
        categoryDao.save(category).block();
        categoryDao.save(updCategory).block();
        underTest.save(item).block();

        Item updatedFields = new Item(null, "upd name", "upd description",
                now().truncatedTo(SECONDS), updCategory, itemVersion);

        // when
        Item expected = new Item(item.getId(), updatedFields.getName(),
                updatedFields.getDescription(), item.getCreated(),
                updatedFields.getCategory(), item.getVersion() + 1);
        underTest.update(item.getId(), updatedFields).block();

        // then
        underTest.get(item.getId())
                .as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();

        // clean up
        underTest.deleteById(item.getId());
        categoryDao.deleteById(category.getId());
        categoryDao.deleteById(updCategory.getId());
    }

    @Test
    public void canDeleteById() {
        // given
        Category category = new Category(null, "category", "desc", null);
        categoryDao.save(category).block();
        Item item = new Item(null, "name", "description",
                now().truncatedTo(SECONDS), category, 1L);
        underTest.save(item).block();

        // when
        underTest.deleteById(item.getId()).block();

        // then
        mongoTemplate
                .find(new Query(Criteria.where("name").is(item.getName()).and("description").is("desc")), Item.class)
                .as(StepVerifier::create)
                .verifyComplete();

        // clean up
        categoryDao.deleteById(category.getId());
    }
}

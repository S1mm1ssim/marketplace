package com.modsensoftware.marketplace.unit.item;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/22/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemDaoTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ItemDao underTest;

    @Autowired
    private CategoryDao categoryDao;

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.itemNotFound}")
    private String itemNotFoundMessage;

    @Test
    public void canSaveItem() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                now().truncatedTo(SECONDS), category, 1L);

        // when
        underTest.save(item);

        // then
        Session session = sessionFactory.openSession();
        Item actual = session.get(Item.class, item.getId());
        Assertions.assertThat(item).isEqualTo(actual);
        session.close();

        // clean up
        deleteItem(item);
    }

    @Test
    public void canGetItemByUuid() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description", now().truncatedTo(SECONDS), category, 1L);
        underTest.save(item);

        // when
        Item actual = underTest.get(item.getId());

        // then
        Assertions.assertThat(actual).isEqualTo(item);

        // clean up
        deleteItem(item);
    }

    @Test
    public void getByNonExistentUuidShouldThrowEntityNotFoundException() {
        // given
        String nonExistentUuid = "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d";
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(UUID.fromString(nonExistentUuid)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format(itemNotFoundMessage, nonExistentUuid));
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
            underTest.save(item);
        }
        // when
        List<Item> firstPage = underTest.getAll(0, Collections.emptyMap());
        List<Item> secondPage = underTest.getAll(1, Collections.emptyMap());

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize);
        Assertions.assertThat(secondPage.size()).isEqualTo(items.size() - pageSize);

        // clean up
        deleteAllItems(items, category);
    }

    @Test
    public void canUpdateItem() {
        // given
        Category category = new Category(null, "another category", "other description", null);
        Category updCategory = new Category(null, "category", "description", null);
        categoryDao.save(category);
        categoryDao.save(updCategory);

        long itemVersion = 1L;
        Item updatedFields = new Item(null, "upd name", "upd description",
                now().truncatedTo(SECONDS), updCategory, itemVersion);
        Item item = new Item(null, "name", "description",
                now().truncatedTo(SECONDS), category, itemVersion);
        underTest.save(item);

        Item expected = new Item(item.getId(), updatedFields.getName(),
                updatedFields.getDescription(), item.getCreated(),
                updatedFields.getCategory(), item.getVersion() + 1);

        // when
        underTest.update(item.getId(), updatedFields);

        // then
        Item actual = underTest.get(item.getId());
        Assertions.assertThat(actual).isEqualTo(expected);

        // clean up
        underTest.deleteById(item.getId());
        deleteAllCategories(List.of(category, updCategory));
    }

    @Test
    @DisplayName("OptimisticLockException should be thrown and only first update invocation should be executed")
    public void onTwoConcurrentUpdateCallsOnlyFirstShouldMakeChanges() throws InterruptedException {
        // given
        Category category = new Category(null, "another category", "other description", null);
        Category updCategory = new Category(null, "category", "description", null);
        categoryDao.save(category);
        categoryDao.save(updCategory);

        long itemVersion = 1L;
        Item updatedFields1 = new Item(null, "upd name", "upd description", null, category, itemVersion);
        Item updatedFields2 = new Item(null, "upd upd name", "upd upd description", null, updCategory, itemVersion);
        Item item = new Item(null, "name", "description", now().truncatedTo(SECONDS), category, itemVersion);
        underTest.save(item);

        Item expected = new Item(item.getId(), updatedFields1.getName(),
                updatedFields1.getDescription(), item.getCreated(),
                updatedFields1.getCategory(), item.getVersion() + 1);

        // when
        // In some cases second update is executed before first
        // and first update is fallen with OptimisticLockException
        List<Item> updatedFields = List.of(updatedFields1, updatedFields2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (Item updatedField : updatedFields) {
            executor.execute(() -> underTest.update(item.getId(), updatedField));
            // In some cases second update was executed before first
            // So Thread.sleep was added to prevent that behavior
            // 5 ms timeout seems reliable as in 30 class runs this test never failed again
            Thread.sleep(5);
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        // then
        Item actual = underTest.get(item.getId());
        Assertions.assertThat(actual).isEqualTo(expected);

        // clean up
        underTest.deleteById(item.getId());
        deleteAllCategories(List.of(category, updCategory));
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        long itemVersion = 1L;
        Item updatedFields = new Item(null, null, null, null, new Category(), itemVersion);
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                now().truncatedTo(SECONDS), category, itemVersion);
        underTest.save(item);

        // when
        underTest.update(item.getId(), updatedFields);

        // then
        Item result = underTest.get(item.getId());
        Assertions.assertThat(result).isEqualTo(item);

        // clean up
        deleteItem(item);
    }

    @Test
    public void canDeleteByUuid() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                now().truncatedTo(SECONDS), category, 1L);
        underTest.save(item);

        // when
        underTest.deleteById(item.getId());

        // then
        Session session = sessionFactory.openSession();
        Item result = session.get(Item.class, item.getId());
        Assertions.assertThat(result).isNull();
        session.close();

        // clean up
        categoryDao.deleteById(category.getId());
    }

    private void deleteItem(Item item) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(item);
        session.delete(item.getCategory());
        transaction.commit();
        session.close();
    }

    private void deleteAllItems(Collection<Item> items, Category category) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        items.forEach(session::delete);
        session.delete(category);
        transaction.commit();
        session.close();
    }

    private void deleteAllCategories(List<Category> categories) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        categories.forEach(session::delete);
        transaction.commit();
        session.close();
    }
}

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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/22/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemDaoTest {

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

    @Test
    public void canSaveItem() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, 1L);
        // when
        underTest.save(item);
        // then
        Session session = sessionFactory.openSession();
        Item result = session.get(Item.class, item.getId());
        Assertions.assertThat(item).isEqualTo(result);
        session.close();
        // clean up
        underTest.deleteById(item.getId());
        categoryDao.deleteById(category.getId());
    }

    @Test
    public void canGetItemByUuid() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, 1L);
        underTest.save(item);
        // when
        Item result = underTest.get(item.getId());
        // then
        Assertions.assertThat(result).isEqualTo(item);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(item);
        session.delete(category);
        transaction.commit();
        session.close();
    }

    @Test
    public void getByNonExistentUuidShouldThrowEntityNotFoundException() {
        // given
        String nonExistentUuid = "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d";
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(UUID.fromString(nonExistentUuid)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format("Item entity with uuid=%s is not present.", nonExistentUuid));
    }

    @Test
    public void canGetAllItemsWithPagination() {
        // given
        List<Item> items = new ArrayList<>();
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        for (int i = 0; i < pageSize + 1; i++) {
            Item item = new Item(null, "name", "description", LocalDateTime.now(), category, 1L);
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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        items.forEach(session::delete);
        session.delete(category);
        transaction.commit();
        session.close();
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
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), updCategory, itemVersion);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, itemVersion);
        underTest.save(item);

        Item expected = new Item(item.getId(), updatedFields.getName(),
                updatedFields.getDescription(), item.getCreated(),
                updatedFields.getCategory(), item.getVersion() + 1);

        // when
        underTest.update(item.getId(), updatedFields);

        // then
        Item result = underTest.get(item.getId());
        Assertions.assertThat(result).isEqualTo(expected);

        // clean up
        underTest.deleteById(item.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(category);
        session.delete(updCategory);
        transaction.commit();
        session.close();
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
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, itemVersion);
        underTest.save(item);

        Item expected = new Item(item.getId(), updatedFields1.getName(),
                updatedFields1.getDescription(), item.getCreated(),
                updatedFields1.getCategory(), item.getVersion() + 1);

        // when
        List<Item> updatedFields = List.of(updatedFields1, updatedFields2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (Item updatedField : updatedFields) {
            executor.execute(() -> underTest.update(item.getId(), updatedField));
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // then
        Item result = underTest.get(item.getId());
        Assertions.assertThat(result).isEqualTo(expected);

        // clean up
        underTest.deleteById(item.getId());
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(category);
        session.delete(updCategory);
        transaction.commit();
        session.close();
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        long itemVersion = 1L;
        Item updatedFields = new Item();
        updatedFields.setVersion(itemVersion);
        updatedFields.setCategory(new Category());

        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, itemVersion);
        underTest.save(item);

        // when
        underTest.update(item.getId(), updatedFields);

        // then
        Item result = underTest.get(item.getId());
        Assertions.assertThat(result).isEqualTo(item);

        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(item);
        session.delete(category);
        transaction.commit();
        session.close();
    }

    @Test
    public void canDeleteByUuid() {
        // given
        Category category = new Category(null, "category", "description", null);
        categoryDao.save(category);
        Item item = new Item(null, "name", "description",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), category, 1L);
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

}

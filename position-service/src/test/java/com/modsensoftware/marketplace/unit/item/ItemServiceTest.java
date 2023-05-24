package com.modsensoftware.marketplace.unit.item;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.dto.mapper.ItemMapper;
import com.modsensoftware.marketplace.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private CategoryDao categoryDao;
    @Mock
    private ItemDao itemDao;

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    private ItemServiceImpl underTest;

    private static final String ITEM_VERSIONS_MISMATCH_MESSAGE
            = "Provided item version does not match with the one in the database";

    @BeforeEach
    void setUp() {
        underTest = new ItemServiceImpl(categoryDao, itemDao, itemMapper);
        ReflectionTestUtils.setField(underTest, "itemVersionsMismatchMessage", ITEM_VERSIONS_MISMATCH_MESSAGE);
    }

    @Test
    public void canUpdateItem() {
        // given
        String id = UUID.randomUUID().toString();
        long version = 1L;
        ItemDto updatedFields = ItemDto.builder().description("description").version(version).build();
        Item item = Item.builder().id(id).version(version).build();
        Item expected = Item.builder().id(id).description("description").version(version + 1).build();
        BDDMockito.given(itemDao.get(id)).willReturn(Mono.just(item));
        BDDMockito.given(itemDao.update(id, itemMapper.toItem(updatedFields))).willReturn(Mono.just(expected));

        // when
        underTest.updateItem(id, updatedFields)
                // then
                .as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    public void canCreateItem() {
        // given
        String categoryId = "12345";
        Category category = Category.builder().id(categoryId).name("category").build();
        ItemDto toBeSaved = ItemDto.builder().name("name").description("descr").version(0L).categoryId(categoryId).build();
        Item expected = Item.builder().name("name").description("descr")
                .category(category).version(0L).build();
        BDDMockito.given(categoryDao.get(categoryId)).willReturn(Mono.just(category));
        BDDMockito.given(itemDao.save(BDDMockito.any())).willReturn(Mono.just(expected));

        // when
        underTest.createItem(toBeSaved)
                // then
                .as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    public void shouldThrowExceptionWhenItemVersionsMismatch() {
        // given
        String id = UUID.randomUUID().toString();
        long version = 1L;
        long differentVersion = 2L;
        ItemDto updatedFields = ItemDto.builder().description("description").version(version).build();
        Item item = Item.builder().id(id).version(differentVersion).build();
        BDDMockito.given(itemDao.get(id)).willReturn(Mono.just(item));

        // when
        Mono<Item> itemMono = underTest.updateItem(id, updatedFields);

        // then
        itemMono.as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable.getClass().equals(OptimisticLockingFailureException.class)
                        && throwable.getMessage().equals(ITEM_VERSIONS_MISMATCH_MESSAGE))
                .verify();
    }
}

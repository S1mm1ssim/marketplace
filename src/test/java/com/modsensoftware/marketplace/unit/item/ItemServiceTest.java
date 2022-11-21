package com.modsensoftware.marketplace.unit.item;

import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.dto.mapper.ItemMapper;
import com.modsensoftware.marketplace.service.impl.ItemServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.OptimisticLockException;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemDao itemDao;

    private final ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    private ItemServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new ItemServiceImpl(itemDao, itemMapper);
    }

    @Test
    public void canUpdateItem() {
        // given
        UUID id = UUID.randomUUID();
        long version = 1L;
        ItemDto updatedFields = new ItemDto();
        updatedFields.setVersion(version);
        updatedFields.setDescription("description");
        Item item = new Item();
        item.setId(id);
        item.setVersion(version);
        BDDMockito.given(itemDao.get(id)).willReturn(item);

        // when
        underTest.updateItem(id, updatedFields);

        // then
        BDDMockito.verify(itemDao).update(id, itemMapper.toItem(updatedFields));
    }

    @Test
    public void shouldThrowExceptionWhenVersionsMismatch() {
        // given
        UUID id = UUID.randomUUID();
        long version = 1L;
        long differentVersion = 2L;
        ItemDto updatedFields = new ItemDto();
        updatedFields.setVersion(version);
        updatedFields.setDescription("description");
        Item item = new Item();
        item.setId(id);
        item.setVersion(differentVersion);
        BDDMockito.given(itemDao.get(id)).willReturn(item);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.updateItem(id, updatedFields))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessage("Provided item version does not match with the one in the database");
        BDDMockito.verify(itemDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }
}

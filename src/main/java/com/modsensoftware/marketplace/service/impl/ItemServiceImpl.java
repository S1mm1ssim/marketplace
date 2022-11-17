package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.dto.mapper.ItemMapper;
import com.modsensoftware.marketplace.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemDao itemDao;
    private final ItemMapper itemMapper;

    @Override
    public Item getItemById(UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching item by id: {}", id);
        }
        return itemDao.get(id);
    }

    @Override
    public List<Item> getAllItems(int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all items");
        }
        return itemDao.getAll(pageNumber);
    }

    @Override
    public void createItem(ItemDto itemDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new item from dto: {}", itemDto);
        }
        Item item = itemMapper.toItem(itemDto);
        item.setCreated(LocalDateTime.now());
        if (log.isDebugEnabled()) {
            log.debug("Mapping result: {}", item);
        }
        itemDao.save(item);
    }

    @Override
    public void deleteItem(UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting item by id: {}", id);
        }
        itemDao.deleteById(id);
    }

    @Override
    public void updateItem(UUID id, ItemDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating item with id: {}\nwith params: {}", id, updatedFields);
        }
        itemDao.update(id, itemMapper.toItem(updatedFields));
    }
}

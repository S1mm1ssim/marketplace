package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.dto.mapper.ItemMapper;
import com.modsensoftware.marketplace.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Collections;
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
        log.debug("Fetching item by id: {}", id);
        return itemDao.get(id);
    }

    @Override
    public List<Item> getAllItems(int pageNumber) {
        log.debug("Fetching all items for page {}", pageNumber);
        return itemDao.getAll(pageNumber, Collections.emptyMap());
    }

    @Override
    public void createItem(ItemDto itemDto) {
        log.debug("Creating new item from dto: {}", itemDto);
        Item item = itemMapper.toItem(itemDto);
        item.setCreated(LocalDateTime.now());
        log.debug("Mapping result: {}", item);
        itemDao.save(item);
    }

    @Override
    public void deleteItem(UUID id) {
        log.debug("Deleting item by id: {}", id);
        itemDao.deleteById(id);
    }

    @Override
    public void updateItem(UUID id, ItemDto updatedFields) {
        log.debug("Updating item with id: {}\nwith params: {}", id, updatedFields);
        Item item = itemDao.get(id);
        if (item.getVersion().equals(updatedFields.getVersion())) {
            log.debug("Item versions match");
            itemDao.update(id, itemMapper.toItem(updatedFields));
        } else {
            log.error("Item versions do not match. Provided: {}, in the database: {}",
                    updatedFields.getVersion(), item.getVersion());
            throw new OptimisticLockException("Provided item version "
                    + "does not match with the one in the database");
        }
    }
}

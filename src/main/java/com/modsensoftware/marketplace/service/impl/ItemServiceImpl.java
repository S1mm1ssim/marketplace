package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.dto.mapper.ItemMapper;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.service.ItemService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class ItemServiceImpl implements ItemService {

    private final ItemDao itemDao;
    private final ItemMapper itemMapper;

    @Override
    public Item getItemById(UUID id) throws EntityNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Fetching item by id: {}", id);
        }
        return itemDao.get(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Item> getAllItems() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all items");
        }
        return itemDao.getAll();
    }

    @Override
    public void createItem(ItemDto itemDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new item: {}", itemDto);
        }
        itemDao.save(itemMapper.toItem(itemDto));
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

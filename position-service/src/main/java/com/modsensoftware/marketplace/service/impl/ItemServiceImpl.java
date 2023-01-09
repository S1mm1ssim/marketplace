package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.dto.mapper.ItemMapper;
import com.modsensoftware.marketplace.service.ItemService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final CategoryDao categoryDao;
    private final ItemDao itemDao;
    private final ItemMapper itemMapper;

    @Value("${exception.message.itemVersionsMismatch}")
    private String itemVersionsMismatchMessage;

    @Override
    public Mono<Item> getItemById(String id) {
        log.debug("Fetching item by id: {}", id);
        return itemDao.get(id);
    }

    @Override
    public Flux<Item> getAllItems(int pageNumber) {
        log.debug("Fetching all items for page {}", pageNumber);
        return itemDao.getAll(pageNumber, Collections.emptyMap());
    }

    @Override
    public Mono<Item> createItem(ItemDto itemDto) {
        log.debug("Creating new item from dto: {}", itemDto);
        Item item = itemMapper.toItem(itemDto);
        item.setCreated(LocalDateTime.now());
        return categoryDao.get(item.getCategory().getId()).map(category -> {
            item.setCategory(category);
            return item;
        }).flatMap(itemDao::save);
    }

    @Override
    public Mono<DeleteResult> deleteItem(String id) {
        log.debug("Deleting item by id: {}", id);
        return itemDao.deleteById(id);
    }

    @Override
    public Mono<Item> updateItem(String id, ItemDto updatedFields) {
        log.debug("Updating item with id: {}\nwith params: {}", id, updatedFields);
        return itemDao.get(id).flatMap(item -> {
            if (item.getVersion().equals(updatedFields.getVersion())) {
                log.debug("Item versions match");
                return itemDao.update(id, itemMapper.toItem(updatedFields));
            } else {
                log.error("Item versions do not match. Provided: {}, in the database: {}",
                        updatedFields.getVersion(), item.getVersion());
                return Mono.error(new OptimisticLockingFailureException(itemVersionsMismatchMessage));
            }
        });
    }
}

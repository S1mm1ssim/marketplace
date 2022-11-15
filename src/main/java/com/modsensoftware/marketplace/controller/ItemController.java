package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping(produces = {"application/json"})
    public List<Item> getAllItems() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all users");
        }
        return itemService.getAllItems();
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Item getItemById(@PathVariable(name = "id") UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching user by id={}", id);
        }
        return itemService.getItemById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createItem(@RequestBody ItemDto itemDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new item: {}", itemDto);
        }
        itemService.createItem(itemDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting item by id: {}", id);
        }
        itemService.deleteItem(id);
    }

    @PutMapping("/{id}")
    public void updateItem(@PathVariable(name = "id") UUID id, @RequestBody ItemDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating item with id: {}\nwith params: {}", id, updatedFields);
        }
        itemService.updateItem(id, updatedFields);
    }
}

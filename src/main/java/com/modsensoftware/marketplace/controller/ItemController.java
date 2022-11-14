package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<List<Item>> getAllItems() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all users");
        }
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Item> getItemById(@PathVariable(name = "id") UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching user by id={}", id);
        }
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createItem(@RequestBody ItemDto itemDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new item: {}", itemDto);
        }
        itemService.createItem(itemDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting item by id: {}", id);
        }
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateItem(@PathVariable(name = "id") UUID id, @RequestBody ItemDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating item with id: {}\nwith params: {}", id, updatedFields);
        }
        itemService.updateItem(id, updatedFields);
        return ResponseEntity.ok().build();
    }
}

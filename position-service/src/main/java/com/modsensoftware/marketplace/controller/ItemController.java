package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.service.ItemService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.PAGE_FILTER_NAME;

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
    public Flux<Item> getAllItems(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber) {
        log.debug("Fetching all items for page {}", pageNumber);
        return itemService.getAllItems(pageNumber);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Mono<Item> getItemById(@PathVariable(name = "id") String id) {
        log.debug("Fetching item by id: {}", id);
        return itemService.getItemById(id);
    }

    @PreAuthorize("hasAnyRole('STORAGE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<Item> createItem(@RequestBody ItemDto itemDto) {
        log.debug("Creating new item from dto: {}", itemDto);
        return itemService.createItem(itemDto);
    }

    @PreAuthorize("hasAnyRole('STORAGE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<DeleteResult> deleteItem(@PathVariable String id) {
        log.debug("Deleting item by id: {}", id);
        return itemService.deleteItem(id);
    }

    @PreAuthorize("hasAnyRole('STORAGE_MANAGER')")
    @PutMapping("/{id}")
    public Mono<Item> updateItem(@PathVariable(name = "id") String id,
                           @RequestBody ItemDto updatedFields) {
        log.debug("Updating item with id: {}\nwith params: {}", id, updatedFields);
        return itemService.updateItem(id, updatedFields);
    }
}

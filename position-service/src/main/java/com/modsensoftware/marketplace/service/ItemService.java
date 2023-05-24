package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.mongodb.client.result.DeleteResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface ItemService {

    Mono<Item> getItemById(String id);

    Flux<Item> getAllItems(int pageNumber);

    Mono<Item> createItem(ItemDto itemDto);

    Mono<DeleteResult> deleteItem(String id);

    Mono<Item> updateItem(String id, ItemDto updatedFields);
}

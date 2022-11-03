package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.ItemDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface ItemService {

    Item getItemById(UUID id) throws EntityNotFoundException;

    List<Item> getAllItems();

    void createItem(ItemDto itemDto);

    void deleteItem(UUID id);

    void updateItem(UUID id, ItemDto updatedFields);
}
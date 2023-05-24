package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.utils.Utils;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.modsensoftware.marketplace.constants.Constants.MONGO_ID_FIELD_NAME;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemDao implements Dao<Item, String> {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final CategoryDao categoryDao;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.itemNotFound}")
    private String itemNotFoundMessage;

    @Override
    public Mono<Item> get(String id) {
        log.debug("Fetching item entity with id {}", id);
        return reactiveMongoTemplate
                .findById(id, Item.class)
                .defaultIfEmpty(new Item())
                .flatMap(item -> {
                    if (item.equals(new Item())) {
                        log.info("No item found for id {}", id);
                        return Mono.error(new EntityNotFoundException(format(itemNotFoundMessage, id)));
                    }
                    log.info("Fetched item {}", item);
                    return Mono.just(item);
                });
    }

    @Override
    public Flux<Item> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all items for page {}", pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Query getAllPaged = new Query().with(pageable);
        return reactiveMongoTemplate.find(getAllPaged, Item.class);
    }

    @Override
    public Mono<Item> save(Item item) {
        log.debug("Saving item entity: {}", item);
        return reactiveMongoTemplate.insert(item);
    }

    @Override
    public Mono<Item> update(String id, Item updatedFields) {
        log.debug("Updating item entity with id {} with values from: {}", id, updatedFields);
        return reactiveMongoTemplate.findById(id, Item.class)
                .flatMap(item -> {
                    Utils.setIfNotNull(updatedFields.getName(), item::setName);
                    Utils.setIfNotNull(updatedFields.getDescription(), item::setDescription);
                    if (updatedFields.getCategory().getId() != null) {
                        return categoryDao.get(updatedFields.getCategory().getId()).flatMap(category -> {
                            item.setCategory(category);
                            return Mono.just(item);
                        });
                    }
                    return Mono.just(item);
                }).flatMap(reactiveMongoTemplate::save);
    }

    @Override
    public Mono<DeleteResult> deleteById(String id) {
        log.debug("Deleting item entity with id: {}", id);
        return reactiveMongoTemplate.remove(new Query(Criteria.where(MONGO_ID_FIELD_NAME).is(id)), Item.class);
    }
}
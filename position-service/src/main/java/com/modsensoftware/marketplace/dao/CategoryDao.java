package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Category;
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
public class CategoryDao implements Dao<Category, String> {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.categoryNotFound}")
    private String categoryNotFoundMessage;

    @Override
    public Mono<Category> get(String id) {
        log.debug("Fetching category entity with id {}", id);
        return reactiveMongoTemplate
                .findById(id, Category.class)
                .defaultIfEmpty(new Category())
                .flatMap(category -> {
                    if (category.equals(new Category())) {
                        log.info("No category found for id {}", id);
                        return Mono.error(new EntityNotFoundException(format(categoryNotFoundMessage, id)));
                    }
                    log.info("Fetched category {}", category);
                    return Mono.just(category);
                });
    }

    @Override
    public Flux<Category> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all categories for page {}", pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Query getAllPaged = new Query().with(pageable);
        return reactiveMongoTemplate.find(getAllPaged, Category.class);
    }

    @Override
    public Mono<Category> save(Category category) {
        log.debug("Saving category entity: {}", category);
        return reactiveMongoTemplate.insert(category);
    }

    @Override
    public Mono<Category> update(String id, Category updatedFields) {
        return reactiveMongoTemplate.findById(id, Category.class)
                .flatMap(toBeUpdated -> {
                    log.debug("Updating category entity with id {} with values from: {}", id, updatedFields);
                    Utils.setIfNotNull(updatedFields.getName(), toBeUpdated::setName);
                    Utils.setIfNotNull(updatedFields.getDescription(), toBeUpdated::setDescription);
                    Category parent = updatedFields.getParent();
                    if (parent != null && parent.getId() != null) {
                        return reactiveMongoTemplate.findById(parent.getId(), Category.class)
                                .flatMap(p -> {
                                    toBeUpdated.setParent(p);
                                    return Mono.just(toBeUpdated);
                                });
                    } else if (parent == null) {
                        toBeUpdated.setParent(null);
                    }
                    return Mono.just(toBeUpdated);
                }).flatMap(reactiveMongoTemplate::save);
    }

    @Override
    public Mono<DeleteResult> deleteById(String id) {
        log.debug("Deleting category entity with id: {}", id);
        return reactiveMongoTemplate.remove(new Query(Criteria.where(MONGO_ID_FIELD_NAME).is(id)), Category.class);
    }
}

package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.Position;
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
public class PositionDao implements Dao<Position, String> {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.positionNotFound}")
    private String positionNotFoundMessage;

    @Override
    public Mono<Position> get(String id) {
        log.debug("Fetching position entity with id {}", id);
        return reactiveMongoTemplate
                .findById(id, Position.class)
                .defaultIfEmpty(new Position())
                .flatMap(position -> {
                    if (position.equals(new Position())) {
                        return Mono.error(new EntityNotFoundException(format(positionNotFoundMessage, id)));
                    }
                    return Mono.just(position);
                });
    }

    @Override
    public Flux<Position> getAll(int pageNumber, Map<String, String> filterProperties) {
        log.debug("Fetching all positions for page {}", pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Query getAllPaged = new Query().with(pageable);
        return reactiveMongoTemplate.find(getAllPaged, Position.class);
    }

    @Override
    public Mono<Position> save(Position position) {
        log.debug("Saving position entity: {}", position);
        return reactiveMongoTemplate.insert(position);
    }

    @Override
    public Mono<Position> update(String id, Position updatedFields) {
        log.debug("Updating position entity with id {} with values from: {}", id, updatedFields);
        return reactiveMongoTemplate.findById(id, Position.class)
                .map(position -> {
                    Utils.setIfNotNull(updatedFields.getAmount(), position::setAmount);
                    Utils.setIfNotNull(updatedFields.getMinAmount(), position::setMinAmount);
                    return position;
                }).flatMap(reactiveMongoTemplate::save);
    }

    @Override
    public Mono<DeleteResult> deleteById(String id) {
        log.debug("Deleting position entity with id: {}", id);
        return reactiveMongoTemplate.remove(new Query(Criteria.where(MONGO_ID_FIELD_NAME).is(id)), Position.class);
    }
}

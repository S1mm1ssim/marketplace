package com.modsensoftware.marketplace.dao;

import com.mongodb.client.result.DeleteResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
public interface Dao<T, ID> {
    Mono<T> get(ID id);

    Flux<T> getAll(int pageNumber, Map<String, String> filterProperties);

    Mono<T> save(T t);

    Mono<T> update(ID id, T updatedFields);

    Mono<DeleteResult> deleteById(ID id);
}

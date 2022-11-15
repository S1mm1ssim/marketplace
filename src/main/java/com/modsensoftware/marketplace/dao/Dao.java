package com.modsensoftware.marketplace.dao;

import java.util.List;
import java.util.Optional;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
public interface Dao<T, R> {
    Optional<T> get(R id);

    List<T> getAll();

    void save(T t);

    void update(R id, T updatedFields);

    void deleteById(R id);
}

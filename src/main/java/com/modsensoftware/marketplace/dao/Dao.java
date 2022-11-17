package com.modsensoftware.marketplace.dao;

import java.util.List;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
public interface Dao<T, R> {
    T get(R id);

    List<T> getAll(int pageNumber, Map<String, String> filterProperties);

    void save(T t);

    void update(R id, T updatedFields);

    void deleteById(R id);
}

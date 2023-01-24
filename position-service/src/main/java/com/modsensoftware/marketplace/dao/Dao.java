package com.modsensoftware.marketplace.dao;

import java.util.List;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
public interface Dao<T, ID> {
    T get(ID id);

    List<T> getAll(int pageNumber, Map<String, String> filterProperties);

    ID save(T t);

    void update(ID id, T updatedFields);

    void deleteById(ID id);
}

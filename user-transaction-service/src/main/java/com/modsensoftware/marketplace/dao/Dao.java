package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.domain.UserTransactionStatus;

import java.util.List;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
public interface Dao<T, ID> {
    List<T> getAll(int pageNumber, Map<String, String> filterProperties);

    void save(T t);

    void updateTransactionStatus(ID transactionId, UserTransactionStatus status);
}

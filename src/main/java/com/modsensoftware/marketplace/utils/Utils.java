package com.modsensoftware.marketplace.utils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
public class Utils {

    public static <V> void setIfNotNull(V value, Consumer<V> setter) {
        if (Objects.nonNull(value)) {
            setter.accept(value);
        }
    }

    public static <R, U> void setIfNotNull(R value1, U value2, BiConsumer<R, U> setter) {
        if (Objects.nonNull(value1) && Objects.nonNull(value2)) {
            setter.accept(value1, value2);
        }
    }

    public static <R, U> void putIfNotNull(R key, U value, BiConsumer<R, U> put) {
        if (Objects.nonNull(key) && Objects.nonNull(value)) {
            put.accept(key, value);
        }
    }

    public static String wrapIn(String value, String symbol) {
        return symbol + value + symbol;
    }
}

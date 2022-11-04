package com.modsensoftware.marketplace.utils;

import java.util.Objects;
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
}

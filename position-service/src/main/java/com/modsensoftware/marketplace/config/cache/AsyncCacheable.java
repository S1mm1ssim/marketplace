package com.modsensoftware.marketplace.config.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author andrey.demyanchik on 1/25/2023
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncCacheable {

    String cacheName() default "";

    String key() default "";
}

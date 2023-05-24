package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

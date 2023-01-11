package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException() {
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

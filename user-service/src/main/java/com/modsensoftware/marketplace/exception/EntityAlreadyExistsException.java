package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/16/2022
 */
public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException() {
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
    }

    public EntityAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

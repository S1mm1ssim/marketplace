package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/16/2022
 */
public class EntityAlreadyExistsException extends RuntimeException {

    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}

package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/28/2022
 */
public class NoVersionProvidedException extends RuntimeException {
    public NoVersionProvidedException(String message) {
        super(message);
    }
}

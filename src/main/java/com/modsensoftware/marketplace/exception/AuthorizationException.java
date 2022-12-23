package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }
}

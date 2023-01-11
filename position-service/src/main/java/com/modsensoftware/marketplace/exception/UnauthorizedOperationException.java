package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}

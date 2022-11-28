package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
public class InsufficientOrderAmountException extends RuntimeException {
    public InsufficientOrderAmountException(String message) {
        super(message);
    }
}

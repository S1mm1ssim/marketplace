package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 11/27/2022
 */
public class InsufficientItemsInStockException extends RuntimeException {
    public InsufficientItemsInStockException(String message) {
        super(message);
    }
}

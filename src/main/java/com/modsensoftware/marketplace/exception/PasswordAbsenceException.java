package com.modsensoftware.marketplace.exception;

/**
 * @author andrey.demyanchik on 12/11/2022
 */
public class PasswordAbsenceException extends RuntimeException {

    public PasswordAbsenceException(String message) {
        super(message);
    }
}

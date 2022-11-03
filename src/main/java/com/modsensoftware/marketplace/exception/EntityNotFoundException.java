package com.modsensoftware.marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends Exception {
}

package com.modsensoftware.marketplace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author andrey.demyanchik on 11/29/2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String INVALID_EMAIL_MESSAGE = "Email must be valid.";
    public static final String INVALID_PASSWORD_LENGTH_MESSAGE = "Password must be at least 8 characters long";
    public static final String NEGATIVE_PAGE_NUMBER_MESSAGE = "Page number can not be negative.";

    public static final long MIN_PAGE_NUMBER = 0L;
    public static final String EMAIL_REGEX = "(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)*";

    public static final String PAGE_FILTER_NAME = "page";
    public static final String DEFAULT_PAGE_NUMBER = "0";

    public static final String ID_PATH_VARIABLE_NAME = "id";

    public static final String INVALID_AMOUNT_MESSAGE = "Amount must be more or equal to 0.01.";
    public static final String INVALID_MIN_AMOUNT_MESSAGE = "Minimal amount must be more or equal to 0.01.";
    public static final String MIN_AMOUNT_VALUE = "0.01";
    public static final String MONGO_ID_FIELD_NAME = "_id";
}

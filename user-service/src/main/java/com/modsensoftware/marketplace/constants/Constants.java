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
    public static final String EMAIL_FILTER_NAME = "email";
    public static final String NAME_FILTER_NAME = "name";
    public static final String CREATED_BETWEEN_FILTER_NAME = "created";
    public static final String COMPANY_ID_FILTER_NAME = "companyId";
    public static final String USERS_CACHE_NAME = "users";
    public static final String SINGLE_USER_CACHE_NAME = "user";
}

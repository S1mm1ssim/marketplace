package com.modsensoftware.marketplace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author andrey.demyanchik on 11/29/2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String INVALID_AMOUNT_MESSAGE = "Amount must be more or equal to 0.01.";
    public static final String INVALID_MIN_AMOUNT_MESSAGE = "Minimal amount must be more or equal to 0.01.";

    public static final String MIN_AMOUNT_VALUE = "0.01";

    public static final String PAGE_FILTER_NAME = "page";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String USER_ID_PATH_VARIABLE_NAME = "userId";
}

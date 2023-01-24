package com.modsensoftware.marketplace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author andrey.demyanchik on 11/29/2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String PAGE_FILTER_NAME = "page";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String EMAIL_FILTER_NAME = "email";
    public static final String NAME_FILTER_NAME = "name";
    public static final String CREATED_BETWEEN_FILTER_NAME = "created";
    public static final String COMPANY_ID_FILTER_NAME = "companyId";
    public static final String USERS_CACHE_NAME = "users";
    public static final String SINGLE_USER_CACHE_NAME = "user";
}

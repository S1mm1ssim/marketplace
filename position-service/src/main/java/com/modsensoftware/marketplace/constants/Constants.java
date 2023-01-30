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
    public static final String MONGO_ID_FIELD_NAME = "_id";
    public static final String CATEGORIES_CACHE_NAME = "categories";
    public static final String SINGLE_CATEGORY_CACHE_NAME = "category";
    public static final String ITEMS_CACHE_NAME = "items";
    public static final String SINGLE_ITEM_CACHE_NAME = "item";
    public static final String POSITIONS_CACHE_NAME = "positions";
    public static final String SINGLE_POSITION_CACHE_NAME = "position";
}

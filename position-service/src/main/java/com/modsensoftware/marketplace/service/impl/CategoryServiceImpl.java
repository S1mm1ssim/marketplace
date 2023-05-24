package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.cache.AsyncCacheable;
import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapper;
import com.modsensoftware.marketplace.service.CategoryService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.modsensoftware.marketplace.constants.Constants.CATEGORIES_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_CATEGORY_CACHE_NAME;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryMapper categoryMapper;

    @AsyncCacheable(cacheName = SINGLE_CATEGORY_CACHE_NAME, key = "#p1")
    @Override
    public Mono<Category> getCategoryById(String id) {
        log.debug("Fetching category by id: {}", id);
        return categoryDao.get(id);
    }

    @AsyncCacheable(cacheName = CATEGORIES_CACHE_NAME)
    @Override
    public Flux<Category> getAllCategories(int pageNumber) {
        log.debug("Fetching all categories for page {}", pageNumber);
        return categoryDao.getAll(pageNumber, Collections.emptyMap());
    }

    @CacheEvict(cacheNames = CATEGORIES_CACHE_NAME, allEntries = true)
    @Override
    public Mono<Category> createCategory(CategoryDto categoryDto) {
        log.debug("Creating new category from dto: {}", categoryDto);
        Category category = categoryMapper.toCategory(categoryDto);
        if (category.getParent() != null) {
            return categoryDao.get(category.getParent().getId()).map(parent -> {
                category.setParent(parent);
                return category;
            }).flatMap(categoryDao::save);
        }
        return categoryDao.save(category);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = SINGLE_CATEGORY_CACHE_NAME, key = "#id"),
            @CacheEvict(cacheNames = CATEGORIES_CACHE_NAME, allEntries = true)
    })
    @Override
    public Mono<DeleteResult> deleteCategory(String id) {
        log.debug("Deleting category by id: {}", id);
        return categoryDao.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = CATEGORIES_CACHE_NAME, allEntries = true),
            @CacheEvict(cacheNames = SINGLE_CATEGORY_CACHE_NAME, key = "#id")
    })
    @Override
    public Mono<Category> updateCategory(String id, CategoryDto updatedFields) {
        log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        return categoryDao.update(id, categoryMapper.toCategory(updatedFields));
    }
}

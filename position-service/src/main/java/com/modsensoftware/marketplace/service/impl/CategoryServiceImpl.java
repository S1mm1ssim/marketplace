package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapper;
import com.modsensoftware.marketplace.service.CategoryService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryMapper categoryMapper;

    @Override
    public Mono<Category> getCategoryById(String id) {
        log.debug("Fetching category by id: {}", id);
        return categoryDao.get(id);
    }

    @Override
    public Flux<Category> getAllCategories(int pageNumber) {
        log.debug("Fetching all categories for page {}", pageNumber);
        return categoryDao.getAll(pageNumber, Collections.emptyMap());
    }

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

    @Override
    public Mono<DeleteResult> deleteCategory(String id) {
        log.debug("Deleting category by id: {}", id);
        return categoryDao.deleteById(id);
    }

    @Override
    public Mono<Category> updateCategory(String id, CategoryDto updatedFields) {
        log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        return categoryDao.update(id, categoryMapper.toCategory(updatedFields));
    }
}

package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.mongodb.client.result.DeleteResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface CategoryService {

    Mono<Category> getCategoryById(String id);

    Flux<Category> getAllCategories(int pageNumber);

    Mono<Category> createCategory(CategoryDto categoryDto);

    Mono<DeleteResult> deleteCategory(String id);

    Mono<Category> updateCategory(String id, CategoryDto updatedFields);
}

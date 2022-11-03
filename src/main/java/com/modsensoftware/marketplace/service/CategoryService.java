package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface CategoryService {

    Category getCategoryById(Long id) throws EntityNotFoundException;

    List<Category> getAllCategories();

    void createCategory(CategoryDto categoryDto);

    void deleteCategory(Long id);

    void updateCategory(Long id, CategoryDto updatedFields);
}

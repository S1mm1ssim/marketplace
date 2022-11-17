package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapper;
import com.modsensoftware.marketplace.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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
    public Category getCategoryById(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching category by id: {}", id);
        }
        return categoryDao.get(id);
    }

    @Override
    public List<Category> getAllCategories(int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all categories");
        }
        return categoryDao.getAll(pageNumber, Collections.emptyMap());
    }

    @Override
    public void createCategory(CategoryDto categoryDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new category from dto: {}", categoryDto);
        }
        Category category = categoryMapper.toCategory(categoryDto);
        if (log.isDebugEnabled()) {
            log.debug("Mapping result: {}", category);
        }
        categoryDao.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting category by id: {}", id);
        }
        categoryDao.deleteById(id);
    }

    @Override
    public void updateCategory(Long id, CategoryDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        }
        categoryDao.update(id, categoryMapper.toCategory(updatedFields));
    }
}

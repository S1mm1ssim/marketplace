package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CategoryDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.dto.mapper.CategoryMapper;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryMapper categoryMapper;

    @Override
    public Category getCategoryById(Long id) throws EntityNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Fetching category by id: {}", id);
        }
        return categoryDao.get(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Category> getAllCategories() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all categories");
        }
        return categoryDao.getAll();
    }

    @Override
    public void createCategory(CategoryDto categoryDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new category: {}", categoryDto);
        }
        categoryDao.save(categoryMapper.toCategory(categoryDto));
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

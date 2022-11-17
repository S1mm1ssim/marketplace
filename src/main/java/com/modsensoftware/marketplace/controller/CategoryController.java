package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private static final String DEFAULT_PAGE_NUMBER = "0";

    @GetMapping(produces = {"application/json"})
    public List<Category> getAllCategories(@RequestParam(name = "page",
            defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all categories");
        }
        return categoryService.getAllCategories(pageNumber);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Category getCategoryById(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching category by id={}", id);
        }
        return categoryService.getCategoryById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createCategory(@RequestBody CategoryDto categoryDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new category: {}", categoryDto);
        }
        categoryService.createCategory(categoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting category by id: {}", id);
        }
        categoryService.deleteCategory(id);
    }

    @PutMapping("/{id}")
    public void updateCategory(@PathVariable(name = "id") Long id,
                               @RequestBody CategoryDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        }
        categoryService.updateCategory(id, updatedFields);
    }
}

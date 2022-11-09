package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<Category>> getAllCategories() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all categories");
        }
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Category> getCategoryById(@PathVariable(name = "id") Long id)
            throws EntityNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Fetching category by id={}", id);
        }
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createCategory(@RequestBody CategoryDto categoryDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new category: {}", categoryDto);
        }
        categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting category by id: {}", id);
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCategory(@PathVariable(name = "id") Long id,
                                               @RequestBody CategoryDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        }
        categoryService.updateCategory(id, updatedFields);
        return ResponseEntity.ok().build();
    }
}

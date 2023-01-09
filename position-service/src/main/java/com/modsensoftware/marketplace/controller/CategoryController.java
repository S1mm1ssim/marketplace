package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.dto.CategoryDto;
import com.modsensoftware.marketplace.service.CategoryService;
import com.mongodb.client.result.DeleteResult;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.PAGE_FILTER_NAME;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping(produces = {"application/json"})
    public Flux<Category> getAllCategories(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber) {
        log.debug("Fetching all categories for page {}", pageNumber);
        return categoryService.getAllCategories(pageNumber);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Mono<Category> getCategoryById(@PathVariable(name = "id") String id) {
        log.debug("Fetching category by id: {}", id);
        return categoryService.getCategoryById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<Category> createCategory(@RequestBody CategoryDto categoryDto) {
        log.debug("Creating new category dto: {}", categoryDto);
        return categoryService.createCategory(categoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<DeleteResult> deleteCategory(@PathVariable(name = "id") String id) {
        log.debug("Deleting category by id: {}", id);
        return categoryService.deleteCategory(id);
    }

    @PutMapping("/{id}")
    public Mono<Category> updateCategory(@PathVariable(name = "id") String id,
                               @RequestBody CategoryDto updatedFields) {
        log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        return categoryService.updateCategory(id, updatedFields);
    }
}

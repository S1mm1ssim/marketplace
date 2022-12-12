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

import javax.validation.constraints.Min;
import java.util.List;

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.ID_PATH_VARIABLE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.MIN_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.NEGATIVE_PAGE_NUMBER_MESSAGE;
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
    public List<Category> getAllCategories(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER)
            @Min(value = MIN_PAGE_NUMBER, message = NEGATIVE_PAGE_NUMBER_MESSAGE) int pageNumber) {
        log.debug("Fetching all categories");
        return categoryService.getAllCategories(pageNumber);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Category getCategoryById(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id) {
        log.debug("Fetching category by id={}", id);
        return categoryService.getCategoryById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createCategory(@RequestBody CategoryDto categoryDto) {
        log.debug("Creating new category dto: {}", categoryDto);
        categoryService.createCategory(categoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id) {
        log.debug("Deleting category by id: {}", id);
        categoryService.deleteCategory(id);
    }

    @PutMapping("/{id}")
    public void updateCategory(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id,
                               @RequestBody CategoryDto updatedFields) {
        log.debug("Updating category with id: {}\nwith params: {}", id, updatedFields);
        categoryService.updateCategory(id, updatedFields);
    }
}

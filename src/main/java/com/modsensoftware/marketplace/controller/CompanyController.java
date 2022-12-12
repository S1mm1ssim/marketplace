package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.service.CompanyService;
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

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.ID_PATH_VARIABLE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.NAME_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.PAGE_FILTER_NAME;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping(produces = {"application/json"})
    public List<Company> getAllCompanies(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(name = EMAIL_FILTER_NAME, required = false) String email,
            @RequestParam(name = NAME_FILTER_NAME, required = false) String name
    ) {
        log.debug("Fetching all companies");
        return companyService.getAllCompanies(pageNumber, email, name);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Company getCompanyById(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id) {
        log.debug("Fetching company by id={}", id);
        return companyService.getCompanyById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createCompany(@RequestBody CompanyDto companyDto) {
        log.debug("Creating new company from dto: {}", companyDto);
        companyService.createCompany(companyDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id) {
        log.debug("Deleting company by id: {}", id);
        companyService.deleteCompany(id);
    }

    @PutMapping("/{id}")
    public void updateCompany(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id,
                              @RequestBody CompanyDto updatedFields) {
        log.debug("Updating company: {}\nwith params: {}", id, updatedFields);
        companyService.updateCompany(id, updatedFields);
    }
}

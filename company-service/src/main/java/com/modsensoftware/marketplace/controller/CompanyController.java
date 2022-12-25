package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.dto.CompanyRequestDto;
import com.modsensoftware.marketplace.dto.CompanyResponseDto;
import com.modsensoftware.marketplace.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import javax.validation.Valid;
import java.util.List;

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
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
    public List<CompanyResponseDto> getAllCompanies(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(name = EMAIL_FILTER_NAME, required = false) String email,
            @RequestParam(name = NAME_FILTER_NAME, required = false) String name
    ) {
        log.debug("Fetching all companies for page {}. "
                + "Filter by email: {}, name: {}", pageNumber, email, name);
        return companyService.getAllCompanies(pageNumber, email, name);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public CompanyResponseDto getCompanyById(@PathVariable(name = "id") Long id) {
        log.debug("Fetching company by id: {}", id);
        return companyService.getCompanyById(id);
    }

    @PreAuthorize("hasAnyRole('DIRECTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createCompany(@Valid @RequestBody CompanyRequestDto companyRequestDto) {
        log.debug("Creating new company from dto: {}", companyRequestDto);
        companyService.createCompany(companyRequestDto);
    }

    @PreAuthorize("hasAnyRole('DIRECTOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable(name = "id") Long id) {
        log.debug("Deleting company by id: {}", id);
        companyService.deleteCompany(id);
    }

    @PreAuthorize("hasAnyRole('DIRECTOR')")
    @PutMapping("/{id}")
    public void updateCompany(@PathVariable(name = "id") Long id,
                              @Valid @RequestBody CompanyRequestDto updatedFields) {
        log.debug("Updating company: {}\nwith params: {}", id, updatedFields);
        companyService.updateCompany(id, updatedFields);
    }
}

package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<Company>> getAllCompanies() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all companies");
        }
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Company> getCompanyById(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching company by id={}", id);
        }
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createCompany(@RequestBody Company company) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new company: {}", company);
        }
        companyService.createCompany(company);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting company by id: {}", id);
        }
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCompany(@PathVariable(name = "id") Long id,
                                              @RequestBody CompanyDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating company: {}\nwith params: {}", id, updatedFields);
        }
        companyService.updateCompany(id, updatedFields);
        return ResponseEntity.ok().build();
    }
}

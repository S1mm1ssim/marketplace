package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface CompanyService {

    Company getCompanyById(Long id);

    List<Company> getAllCompanies();

    void createCompany(Company company);

    void deleteCompany(Long id);

    void updateCompany(Long id, CompanyDto updatedFields);
}

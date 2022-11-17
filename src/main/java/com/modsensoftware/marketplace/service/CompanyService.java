package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface CompanyService {

    Company getCompanyById(Long id);

    List<Company> getAllCompanies(int pageNumber);

    void createCompany(CompanyDto companyDto);

    void deleteCompany(Long id);

    void updateCompany(Long id, CompanyDto updatedFields);
}

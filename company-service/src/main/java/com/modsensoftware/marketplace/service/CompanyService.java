package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.CompanyRequest;
import com.modsensoftware.marketplace.dto.CompanyResponse;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface CompanyService {

    CompanyResponse getCompanyById(Long id);

    List<CompanyResponse> getAllCompanies(int pageNumber, String email, String name);

    void createCompany(CompanyRequest companyRequest);

    void deleteCompany(Long id);

    void updateCompany(Long id, CompanyRequest updatedFields);
}

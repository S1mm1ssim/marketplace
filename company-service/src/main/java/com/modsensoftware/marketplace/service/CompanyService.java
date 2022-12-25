package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.CompanyRequestDto;
import com.modsensoftware.marketplace.dto.CompanyResponseDto;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface CompanyService {

    CompanyResponseDto getCompanyById(Long id);

    List<CompanyResponseDto> getAllCompanies(int pageNumber, String email, String name);

    void createCompany(CompanyRequestDto companyRequestDto);

    void deleteCompany(Long id);

    void updateCompany(Long id, CompanyRequestDto updatedFields);
}

package com.modsensoftware.marketplace.dto;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyRequestDto;
import com.modsensoftware.marketplace.dto.CompanyResponseDto;
import org.mapstruct.Mapper;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(CompanyRequestDto companyRequestDto);

    CompanyResponseDto toCompanyResponseDto(Company company);
}

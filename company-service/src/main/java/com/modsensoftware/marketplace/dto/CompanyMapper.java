package com.modsensoftware.marketplace.dto;

import com.modsensoftware.marketplace.domain.Company;
import org.mapstruct.Mapper;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(CompanyRequest companyRequest);

    CompanyResponse toCompanyResponseDto(Company company);
}

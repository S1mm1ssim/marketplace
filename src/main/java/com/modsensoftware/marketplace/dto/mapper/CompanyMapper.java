package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import org.mapstruct.Mapper;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(CompanyDto companyDto);
}

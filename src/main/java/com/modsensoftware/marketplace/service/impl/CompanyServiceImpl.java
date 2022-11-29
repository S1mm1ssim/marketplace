package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.dto.mapper.CompanyMapper;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.service.CompanyService;
import com.modsensoftware.marketplace.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyDao companyDao;
    private final CompanyMapper companyMapper;

    @Override
    public Company getCompanyById(Long id) {
        log.debug("Fetching company by id: {}", id);
        return companyDao.get(id);
    }

    @Override
    public List<Company> getAllCompanies(int pageNumber, String email, String name) {
        log.debug("Fetching all companies for page {}. Filter by email: {} and name: {}",
                pageNumber, email, name);
        Map<String, String> filterProperties = new HashMap<>();
        Utils.putIfNotNull("email", email, filterProperties::put);
        Utils.putIfNotNull("name", name, filterProperties::put);
        return companyDao.getAll(pageNumber, filterProperties);
    }

    @Override
    public void createCompany(CompanyDto companyDto) {
        log.debug("Creating new company from dto: {}", companyDto);
        if (!companyDao.existsByEmail(companyDto.getEmail())) {
            Company company = companyMapper.toCompany(companyDto);
            company.setCreated(LocalDateTime.now());
            log.debug("Mapping result: {}", company);
            companyDao.save(company);
        } else {
            throw new EntityAlreadyExistsException(format("Company with email %s already exists",
                    companyDto.getEmail()));
        }
    }

    @Override
    public void deleteCompany(Long id) {
        log.debug("Deleting company by id: {}", id);
        companyDao.deleteById(id);
    }

    @Override
    public void updateCompany(Long id, CompanyDto company) {
        log.debug("Updating company with id: {}\nwith params: {}", id, company);
        companyDao.update(id, companyMapper.toCompany(company));
    }
}

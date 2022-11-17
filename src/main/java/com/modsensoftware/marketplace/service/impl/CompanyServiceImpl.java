package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.dto.mapper.CompanyMapper;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        if (log.isDebugEnabled()) {
            log.debug("Fetching company by id: {}", id);
        }
        return companyDao.get(id);
    }

    @Override
    public List<Company> getAllCompanies(int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all companies");
        }
        return companyDao.getAll(pageNumber);
    }

    @Override
    public void createCompany(CompanyDto companyDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new company from dto: {}", companyDto);
        }
        if (!companyDao.existsByEmail(companyDto.getEmail())) {
            Company company = companyMapper.toCompany(companyDto);
            company.setCreated(LocalDateTime.now());
            if (log.isDebugEnabled()) {
                log.debug("Mapping result: {}", company);
            }
            companyDao.save(company);
        } else {
            throw new EntityAlreadyExistsException(format("Company with email %s already exists",
                    companyDto.getEmail()));
        }
    }

    @Override
    public void deleteCompany(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting company by id: {}", id);
        }
        companyDao.deleteById(id);
    }

    @Override
    public void updateCompany(Long id, CompanyDto company) {
        if (log.isDebugEnabled()) {
            log.debug("Updating company with id: {}\nwith params: {}", id, company);
        }
        companyDao.update(id, companyMapper.toCompany(company));
    }
}

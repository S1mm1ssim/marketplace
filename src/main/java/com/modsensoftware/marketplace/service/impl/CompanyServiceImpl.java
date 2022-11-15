package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.dto.mapper.CompanyMapper;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return companyDao.get(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Company> getAllCompanies() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all companies");
        }
        return companyDao.getAll();
    }

    @Override
    public void createCompany(Company company) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new company: {}", company);
        }
        companyDao.save(company);
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

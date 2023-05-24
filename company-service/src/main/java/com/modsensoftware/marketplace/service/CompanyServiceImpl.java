package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyMapper;
import com.modsensoftware.marketplace.dto.CompanyRequest;
import com.modsensoftware.marketplace.dto.CompanyResponse;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.modsensoftware.marketplace.constants.Constants.COMPANIES_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.NAME_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_COMPANY_CACHE_NAME;
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

    @Value("${exception.message.companyEmailTaken}")
    private String companyEmailTakenMessage;

    @Cacheable(cacheNames = SINGLE_COMPANY_CACHE_NAME, key = "#id")
    @Override
    public CompanyResponse getCompanyById(Long id) {
        log.debug("Fetching company by id: {}", id);
        return companyMapper.toCompanyResponseDto(companyDao.get(id));
    }

    @Cacheable(cacheNames = COMPANIES_CACHE_NAME)
    @Override
    public List<CompanyResponse> getAllCompanies(int pageNumber, String email, String name) {
        log.debug("Fetching all companies for page {}. Filter by email: {} and name: {}",
                pageNumber, email, name);
        Map<String, String> filterProperties = new HashMap<>();
        Utils.putIfNotNull(EMAIL_FILTER_NAME, email, filterProperties::put);
        Utils.putIfNotNull(NAME_FILTER_NAME, name, filterProperties::put);
        return companyDao.getAll(pageNumber, filterProperties).stream()
                .map(companyMapper::toCompanyResponseDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(cacheNames = COMPANIES_CACHE_NAME, allEntries = true)
    @Override
    public void createCompany(CompanyRequest companyRequest) {
        log.debug("Creating new company from dto: {}", companyRequest);
        if (!companyDao.existsByEmail(companyRequest.getEmail())) {
            Company company = companyMapper.toCompany(companyRequest);
            company.setCreated(LocalDateTime.now());
            company.setIsDeleted(false);
            log.debug("Mapping result: {}", company);
            companyDao.save(company);
        } else {
            throw new EntityAlreadyExistsException(format(companyEmailTakenMessage,
                    companyRequest.getEmail()));
        }
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = SINGLE_COMPANY_CACHE_NAME, key = "#id"),
            @CacheEvict(cacheNames = COMPANIES_CACHE_NAME, allEntries = true)
    })
    @Override
    public void deleteCompany(Long id) {
        log.debug("Deleting company by id: {}", id);
        companyDao.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = COMPANIES_CACHE_NAME, allEntries = true),
            @CacheEvict(cacheNames = SINGLE_COMPANY_CACHE_NAME, key = "#id")
    })
    @Override
    public void updateCompany(Long id, CompanyRequest company) {
        log.debug("Updating company with id: {}\nwith params: {}", id, company);
        companyDao.update(id, companyMapper.toCompany(company));
    }
}

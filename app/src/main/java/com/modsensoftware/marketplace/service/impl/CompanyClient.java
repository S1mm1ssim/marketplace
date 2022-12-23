package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.feign.FeignErrorHandler;
import com.modsensoftware.marketplace.config.feign.OAuthFeignConfig;
import com.modsensoftware.marketplace.dto.Company;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author andrey.demyanchik on 12/19/2022
 */
@FeignClient(value = "COMPANY-SERVICE", path = "/api/v1/companies",
        configuration = {OAuthFeignConfig.class, FeignErrorHandler.class})
public interface CompanyClient {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    List<Company> getCompanies();

    @RequestMapping(value = "/{id}", produces = {"application/json"}, method = RequestMethod.GET)
    Company getCompanyById(@PathVariable(name = "id") Long id);
}

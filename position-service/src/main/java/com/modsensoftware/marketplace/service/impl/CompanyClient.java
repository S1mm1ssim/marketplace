package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.FeignErrorHandler;
import com.modsensoftware.marketplace.dto.Company;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static com.modsensoftware.marketplace.constants.Constants.ID_PATH_VARIABLE_NAME;

/**
 * @author andrey.demyanchik on 12/19/2022
 */
@FeignClient(value = "COMPANY-SERVICE", path = "/api/v1/companies",
        configuration = {FeignErrorHandler.class})
public interface CompanyClient {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    List<Company> getCompanies();

    @RequestMapping(value = "/{id}", produces = {"application/json"}, method = RequestMethod.GET)
    Company getCompanyById(@PathVariable(name = ID_PATH_VARIABLE_NAME) Long id);
}

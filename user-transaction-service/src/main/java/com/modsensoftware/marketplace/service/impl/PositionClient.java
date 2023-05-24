package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.FeignErrorHandler;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author andrey.demyanchik on 12/27/2022
 */
@FeignClient(name = "POSITION-SERVICE", path = "/api/v1/positions",
        configuration = {FeignErrorHandler.class})
public interface PositionClient {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    PositionResponse getPositionById(@PathVariable(name = "id") String id);
}

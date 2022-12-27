package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.FeignErrorHandler;
import com.modsensoftware.marketplace.dto.request.PositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

/**
 * @author andrey.demyanchik on 12/27/2022
 */
@FeignClient(name = "APP", contextId = "APP-position-client", path = "/api/v1/positions",
        configuration = {FeignErrorHandler.class})
public interface PositionClient {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    PositionResponseDto getPositionById(@PathVariable(name = "id") Long id);

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    void updatePosition(@PathVariable(name = "id") Long id, @Valid @RequestBody PositionRequestDto updatedFields);
}

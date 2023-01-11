package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.FeignErrorHandler;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

import static com.modsensoftware.marketplace.constants.Constants.ID_PATH_VARIABLE_NAME;

/**
 * @author andrey.demyanchik on 1/3/2023
 */
@FeignClient(value = "USER-SERVICE", path = "/api/v1/users",
        configuration = {FeignErrorHandler.class})
public interface UserClient {

    @RequestMapping(value = "{id}", produces = {"application/json"}, method = RequestMethod.GET)
    UserResponse getUserById(@PathVariable(name = ID_PATH_VARIABLE_NAME) UUID id);
}

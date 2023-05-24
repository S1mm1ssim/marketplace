package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.FeignErrorHandler;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

/**
 * @author andrey.demyanchik on 12/26/2022
 */
@FeignClient(
        value = "${feign-clients.user-client.name}",
        url = "${feign-clients.user-client.url}",
        path = "${feign-clients.user-client.path}",
        configuration = {FeignErrorHandler.class}
)
public interface UserClient {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    UserResponse getUserById(@PathVariable(name = "id") UUID id);
}

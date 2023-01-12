package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.ReactiveFeignConfig;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 1/3/2023
 */
@ReactiveFeignClient(
        value = "${reactive-feign-clients.user-client.name}",
        url = "${reactive-feign-clients.user-client.url}",
        path = "${reactive-feign-clients.user-client.path}",
        configuration = {ReactiveFeignConfig.class}
)
public interface UserClient {

    @RequestMapping(value = "{id}", produces = {"application/json"}, method = RequestMethod.GET)
    Mono<UserResponseDto> getUserById(@PathVariable(name = "id") String id);
}

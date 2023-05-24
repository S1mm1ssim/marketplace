package com.modsensoftware.marketplace.config;

import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import feign.RetryableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactivefeign.ReactiveOptions;
import reactivefeign.client.statushandler.ReactiveStatusHandler;
import reactivefeign.client.statushandler.ReactiveStatusHandlers;
import reactivefeign.webclient.WebReactiveOptions;

import java.time.Instant;

/**
 * @author andrey.demyanchik on 1/5/2023
 */
@Configuration
public class ReactiveFeignConfig {

    @Value("${reactive-feign.http-client.read-timeout}")
    private int readTimeout;
    @Value("${reactive-feign.http-client.write-timeout}")
    private int writeTimeout;
    @Value("${reactive-feign.http-client.connect-timeout}")
    private int connectTimeout;
    @Value("${reactive-feign.http-client.response-timeout}")
    private int responseTimeout;

    @Bean
    public ReactiveStatusHandler internalServerErrorHandler() {
        return ReactiveStatusHandlers.throwOnStatus(
                (status) -> (status == HttpStatus.INTERNAL_SERVER_ERROR.value()),
                (methodKey, response) ->
                        new RetryableException(response.status(), "", null,
                                java.sql.Date.from(Instant.EPOCH), null)
        );
    }

    @Bean
    public ReactiveStatusHandler notFoundHandler() {
        return ReactiveStatusHandlers.throwOnStatus(
                (status) -> (status == HttpStatus.NOT_FOUND.value()),
                (methodKey, response) ->
                        new EntityNotFoundException("Entity not found")
        );
    }

    @Bean
    public ReactiveOptions reactiveOptions() {
        return new WebReactiveOptions.Builder()
                .setReadTimeoutMillis(readTimeout)
                .setWriteTimeoutMillis(writeTimeout)
                .setResponseTimeoutMillis(responseTimeout)
                .setConnectTimeoutMillis(connectTimeout)
                .build();
    }
}

package com.modsensoftware.marketplace.config;

import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author andrey.demyanchik on 12/21/2022
 */
@Slf4j
public class FeignErrorHandler implements ErrorDecoder {

    private static final int NOT_FOUND_STATUS = 404;

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == NOT_FOUND_STATUS) {
            log.error("Company entity not found");
            return new EntityNotFoundException("Not Found: Company entity not found");
        }
        return errorDecoder.decode(methodKey, response);
    }
}

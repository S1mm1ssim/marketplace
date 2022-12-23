package com.modsensoftware.marketplace.config.feign;

import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;


/**
 * @author andrey.demyanchik on 12/21/2022
 */
@Slf4j
public class FeignErrorHandler implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new EntityAlreadyExistsException("Bad Request");
            case 404:
                log.error("Company entity not found");
                return new EntityNotFoundException("Not Found: Company entity not found");
            default:
                return errorDecoder.decode(methodKey, response);
        }
    }
}

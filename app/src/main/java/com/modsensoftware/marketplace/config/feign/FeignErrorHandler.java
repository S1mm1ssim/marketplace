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

    private static final int BAD_REQUEST_STATUS = 400;
    private static final int NOT_FOUND_STATUS = 404;

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case BAD_REQUEST_STATUS:
                return new EntityAlreadyExistsException("Bad Request");
            case NOT_FOUND_STATUS:
                log.error("Company entity not found");
                return new EntityNotFoundException("Not Found: Company entity not found");
            default:
                return errorDecoder.decode(methodKey, response);
        }
    }
}

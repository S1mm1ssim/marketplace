package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequest;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.service.PositionService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.ID_PATH_VARIABLE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.MIN_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.NEGATIVE_PAGE_NUMBER_MESSAGE;
import static com.modsensoftware.marketplace.constants.Constants.PAGE_FILTER_NAME;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionService positionService;

    @GetMapping(produces = {"application/json"})
    public Flux<PositionResponse> getAllPositions(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER)
            @Min(value = MIN_PAGE_NUMBER, message = NEGATIVE_PAGE_NUMBER_MESSAGE) int pageNumber) {
        log.debug("Fetching all positions for page {}", pageNumber);
        return positionService.getAllPositions(pageNumber);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Mono<PositionResponse> getPositionById(@PathVariable(name = ID_PATH_VARIABLE_NAME) String id) {
        log.debug("Fetching position by id: {}", id);
        return positionService.getPositionById(id);
    }

    @PreAuthorize("hasAnyRole('STORAGE_MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<Position> createPosition(@Valid @RequestBody CreatePositionRequest createPositionRequest,
                                         Authentication authentication) {
        log.debug("Creating new position from dto: {}", createPositionRequest);
        return positionService.createPosition(createPositionRequest, authentication);
    }

    @PreAuthorize("hasAnyRole('STORAGE_MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<DeleteResult> deletePosition(@PathVariable(name = ID_PATH_VARIABLE_NAME) String id,
                               Authentication authentication) {
        log.debug("Deleting position by id: {}", id);
        return positionService.deletePosition(id, authentication);
    }

    @PreAuthorize("hasAnyRole('STORAGE_MANAGER')")
    @PutMapping("/{id}")
    public Mono<Position> updatePosition(@PathVariable(name = ID_PATH_VARIABLE_NAME) String id,
                                         @Valid @RequestBody UpdatePositionRequest updatedFields,
                                         Authentication authentication) {
        log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        return positionService.updatePosition(id, updatedFields, authentication);
    }
}

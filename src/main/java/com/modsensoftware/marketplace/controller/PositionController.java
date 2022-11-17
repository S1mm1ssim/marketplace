package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

import java.util.List;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

    private final PositionService positionService;

    private static final String DEFAULT_PAGE_NUMBER = "0";

    @GetMapping(produces = {"application/json"})
    public List<Position> getAllPositions(@RequestParam(name = "page",
            defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all positions");
        }
        return positionService.getAllPositions(pageNumber);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public Position getPositionById(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching position by id={}", id);
        }
        return positionService.getPositionById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createPosition(@RequestBody PositionDto positionDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new position from dto: {}", positionDto);
        }
        positionService.createPosition(positionDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletePosition(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting position by id: {}", id);
        }
        positionService.deletePosition(id);
    }

    @PutMapping("/{id}")
    public void updatePosition(@PathVariable Long id, @RequestBody PositionDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        }
        positionService.updatePosition(id, updatedFields);
    }
}

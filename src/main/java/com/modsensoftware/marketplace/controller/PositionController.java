package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping(produces = {"application/json"})
    public ResponseEntity<List<Position>> getAllPositions() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all positions");
        }
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public ResponseEntity<Position> getPositionById(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching position by id={}", id);
        }
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping
    public ResponseEntity<Void> createPosition(@RequestBody PositionDto positionDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new position: {}", positionDto);
        }
        positionService.createPosition(positionDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable(name = "id") Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting position by id: {}", id);
        }
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePosition(@PathVariable Long id, @RequestBody PositionDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        }
        positionService.updatePosition(id, updatedFields);
        return ResponseEntity.ok().build();
    }
}

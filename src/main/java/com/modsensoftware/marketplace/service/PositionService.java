package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    Position getPositionById(Long id) throws EntityNotFoundException;

    List<Position> getAllPositions();

    void createPosition(PositionDto positionDto);

    void deletePosition(Long id);

    void updatePosition(Long id, PositionDto updatedFields);
}

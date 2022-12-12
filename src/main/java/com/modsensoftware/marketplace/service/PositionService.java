package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    Position getPositionById(Long id);

    List<Position> getAllPositions(int pageNumber);

    void createPosition(PositionDto positionDto);

    void deletePosition(Long id);

    void updatePosition(Long id, PositionDto updatedFields);
}

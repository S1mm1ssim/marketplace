package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.PositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    PositionResponseDto getPositionById(Long id);

    List<PositionResponseDto> getAllPositions(int pageNumber);

    void createPosition(PositionRequestDto positionRequestDto);

    void deletePosition(Long id);

    void updatePosition(Long id, PositionRequestDto updatedFields);
}

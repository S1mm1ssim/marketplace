package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.CreatePositionRequestDto;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    PositionResponseDto getPositionById(Long id);

    List<PositionResponseDto> getAllPositions(int pageNumber);

    Long createPosition(CreatePositionRequestDto createPositionRequestDto, Authentication authentication);

    void deletePosition(Long id, Authentication authentication);

    void updatePosition(Long id, UpdatePositionRequestDto updatedFields, Authentication authentication);
}

package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.CreatePositionRequest;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    PositionResponse getPositionById(Long id);

    List<PositionResponse> getAllPositions(int pageNumber);

    Long createPosition(CreatePositionRequest createPositionRequest, Authentication authentication);

    void deletePosition(Long id, Authentication authentication);

    void updatePosition(Long id, UpdatePositionRequest updatedFields, Authentication authentication);
}

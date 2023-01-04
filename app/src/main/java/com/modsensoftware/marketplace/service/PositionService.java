package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.PositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    PositionResponse getPositionById(Long id);

    List<PositionResponse> getAllPositions(int pageNumber);

    void createPosition(PositionRequest positionRequest);

    void deletePosition(Long id);

    void updatePosition(Long id, PositionRequest updatedFields);
}

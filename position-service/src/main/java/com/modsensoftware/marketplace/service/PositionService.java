package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequest;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.mongodb.client.result.DeleteResult;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    Mono<PositionResponse> getPositionById(String id);

    Flux<PositionResponse> getAllPositions(int pageNumber);

    Mono<Position> createPosition(CreatePositionRequest createPositionRequest, Authentication authentication);

    Mono<DeleteResult> deletePosition(String id, Authentication authentication);

    Mono<Position> updatePosition(String id, UpdatePositionRequest updatedFields, Authentication authentication);
}

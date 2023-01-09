package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequestDto;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.mongodb.client.result.DeleteResult;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface PositionService {

    Mono<PositionResponseDto> getPositionById(String id);

    Flux<PositionResponseDto> getAllPositions(int pageNumber);

    Mono<Position> createPosition(CreatePositionRequestDto createPositionRequestDto, Authentication authentication);

    Mono<DeleteResult> deletePosition(String id, Authentication authentication);

    Mono<Position> updatePosition(String id, UpdatePositionRequestDto updatedFields, Authentication authentication);
}

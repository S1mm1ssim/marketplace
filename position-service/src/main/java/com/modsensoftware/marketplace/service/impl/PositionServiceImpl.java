package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.cache.AsyncCacheable;
import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequestDto;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.exception.UnauthorizedOperationException;
import com.modsensoftware.marketplace.service.PositionService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.modsensoftware.marketplace.constants.Constants.POSITIONS_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_POSITION_CACHE_NAME;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionDao positionDao;
    private final ItemDao itemDao;
    private final PositionMapper positionMapper;
    private final UserClient userClient;

    @Value("${exception.message.noItemVersionProvided}")
    private String noItemVersionProvidedMessage;
    @Value("${exception.message.positionCreatedByAnotherPersonMessage}")
    private String positionCreatedByAnotherPersonMessage;
    @Value("${exception.message.creatorNotFound}")
    private String creatorNotFoundMessage;

    @AsyncCacheable(cacheName = SINGLE_POSITION_CACHE_NAME, key = "#p1")
    @Override
    public Mono<PositionResponseDto> getPositionById(String id) {
        log.debug("Fetching position by id: {}", id);
        return positionDao.get(id).flatMap(position -> {
            log.debug("Fetching user by id: {}", position.getCreatedBy());
            return userClient.getUserById(position.getCreatedBy())
                    .onErrorMap(error -> new EntityNotFoundException(creatorNotFoundMessage))
                    .map(user -> positionMapper.toResponseDto(position, user));
        });
    }

    @AsyncCacheable(cacheName = POSITIONS_CACHE_NAME)
    @Override
    public Flux<PositionResponseDto> getAllPositions(int pageNumber) {
        log.debug("Fetching all positions for page {}", pageNumber);
        return positionDao.getAll(pageNumber, Collections.emptyMap())
                .flatMap(position -> userClient.getUserById(position.getCreatedBy())
                        .doOnError(error -> log.trace("{}", creatorNotFoundMessage, error))
                        .onErrorResume(e -> Mono.empty())
                        .map(user -> positionMapper.toResponseDto(position, user))
                );
    }

    @CacheEvict(cacheNames = POSITIONS_CACHE_NAME, allEntries = true)
    @Override
    public Mono<Position> createPosition(CreatePositionRequestDto createPositionRequestDto, Authentication authentication) {
        log.debug("Creating new position from dto: {}", createPositionRequestDto);
        if (createPositionRequestDto.getItemVersion() == null) {
            log.error("Provided createPositionRequestDto didn't contain item's version");
            return Mono.error(new NoVersionProvidedException(format(noItemVersionProvidedMessage,
                    createPositionRequestDto.getItemId())));
        }
        // Authentication#getName maps to the JWT’s sub property, if one is present. Keycloak by default returns user id
        return userClient.getUserById(authentication.getName()).flatMap(user -> {
            Position position = positionMapper.toPosition(createPositionRequestDto, user);
            position.setCreated(LocalDateTime.now());
            return itemDao.get(position.getItem().getId()).map(item -> {
                position.setItem(item);
                return position;
            }).flatMap(positionDao::save);
        });
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = SINGLE_POSITION_CACHE_NAME, key = "#id"),
            @CacheEvict(cacheNames = POSITIONS_CACHE_NAME, allEntries = true)
    })
    @Override
    public Mono<DeleteResult> deletePosition(String id, Authentication authentication) {
        return positionDao.get(id).flatMap(position -> {
            // Authentication#getName maps to the JWT’s sub property, if one is present. Keycloak by default returns user id
            if (position.getCreatedBy().equals(authentication.getName())) {
                log.debug("Deleting position by id: {}", id);
                return positionDao.deleteById(id);
            } else {
                log.error("Attempt to delete position with id {} was made by not the same person who created it", id);
                return Mono.error(new UnauthorizedOperationException(positionCreatedByAnotherPersonMessage));
            }
        });
    }

    @Override
    public Mono<Position> updatePosition(String id, UpdatePositionRequestDto updatedFields, Authentication authentication) {
        log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        return positionDao.get(id).flatMap(position -> {
            // Authentication#getName maps to the JWT’s sub property, if one is present. Keycloak by default returns user id
            if (position.getCreatedBy().equals(authentication.getName())) {
                return positionDao.update(id, positionMapper.toPosition(updatedFields));
            } else {
                log.error("Attempt to update position with id {} was made by not the same person who created it", id);
                return Mono.error(new UnauthorizedOperationException(positionCreatedByAnotherPersonMessage));
            }
        });
    }
}

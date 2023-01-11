package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequest;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.exception.UnauthorizedOperationException;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionDao positionDao;
    private final PositionMapper positionMapper;
    private final CompanyClient companyClient;
    private final UserClient userClient;

    @Value("${exception.message.noItemVersionProvided}")
    private String noItemVersionProvidedMessage;
    @Value("${exception.message.positionCreatedByAnotherPersonMessage}")
    private String positionCreatedByAnotherPersonMessage;

    @Override
    public PositionResponse getPositionById(Long id) {
        log.debug("Fetching position by id: {}", id);
        Position position = positionDao.get(id);
        log.debug("Fetching user by id: {}", position.getCreatedBy());
        UserResponse user = userClient.getUserById(position.getCreatedBy());
        return positionMapper.toResponseDto(position, user);
    }

    @Override
    public List<PositionResponse> getAllPositions(int pageNumber) {
        log.debug("Fetching all positions for page {}", pageNumber);
        List<Position> positions = positionDao.getAll(pageNumber, Collections.emptyMap());
        List<Company> companies = companyClient.getCompanies();
        // Creating map of companyId:company pairs
        Map<Long, Company> companyIdSelfMap = companies.stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));
        return positions.stream()
                .filter(position -> companyIdSelfMap.containsKey(position.getCompanyId()))
                .map(position -> {
                    UserResponse user = userClient.getUserById(position.getCreatedBy());
                    return positionMapper.toResponseDto(position, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long createPosition(CreatePositionRequest createPositionRequest, Authentication authentication) {
        log.debug("Creating new position from dto: {}", createPositionRequest);
        if (createPositionRequest.getItemVersion() == null) {
            log.error("Provided createPositionRequest didn't contain item's version");
            throw new NoVersionProvidedException(format(noItemVersionProvidedMessage,
                    createPositionRequest.getItemId()));
        }
        // Authentication#getName maps to the JWT’s sub property, if one is present. Keycloak by default returns user id
        UserResponse user = userClient.getUserById(UUID.fromString(authentication.getName()));
        Position position = positionMapper.toPosition(createPositionRequest, user);
        position.setCreated(LocalDateTime.now());
        log.debug("Mapping result: {}", position);
        return positionDao.save(position);
    }

    @Override
    public void deletePosition(Long id, Authentication authentication) {
        Position position = positionDao.get(id);
        // Authentication#getName maps to the JWT’s sub property, if one is present. Keycloak by default returns user id
        if (position.getCreatedBy().equals(UUID.fromString(authentication.getName()))) {
            log.debug("Deleting position by id: {}", id);
            positionDao.deleteById(id);
        } else {
            log.error("Attempt to delete position with id {} was made by not the same person who created it", id);
            throw new UnauthorizedOperationException(positionCreatedByAnotherPersonMessage);
        }
    }

    @Override
    public void updatePosition(Long id, UpdatePositionRequest updatedFields, Authentication authentication) {
        log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        Position position = positionDao.get(id);
        // Authentication#getName maps to the JWT’s sub property, if one is present. Keycloak by default returns user id
        if (position.getCreatedBy().equals(UUID.fromString(authentication.getName()))) {
            positionDao.update(id, positionMapper.toPosition(updatedFields));
        } else {
            log.error("Attempt to update position with id {} was made by not the same person who created it", id);
            throw new UnauthorizedOperationException(positionCreatedByAnotherPersonMessage);
        }
    }
}

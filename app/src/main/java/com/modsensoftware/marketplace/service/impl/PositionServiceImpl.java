package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.dto.request.PositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final UserDao userDao;
    private final PositionMapper positionMapper;
    private final CompanyClient companyClient;

    @Value("${exception.message.positionVersionsMismatch}")
    private String positionVersionsMismatch;
    @Value("${exception.message.noItemVersionProvided}")
    private String noItemVersionProvidedMessage;

    @Override
    public PositionResponseDto getPositionById(Long id) {
        log.debug("Fetching company by id: {}", id);
        Position position = positionDao.get(id);
        Company positionCompany = companyClient.getCompanyById(position.getCompanyId());
        Company userCompany = companyClient.getCompanyById(position.getCreatedBy().getCompanyId());
        return positionMapper.toResponseDto(position, positionCompany, userCompany);
    }

    @Override
    public List<PositionResponseDto> getAllPositions(int pageNumber) {
        log.debug("Fetching all positions for page {}", pageNumber);
        List<Position> positions = positionDao.getAll(pageNumber, Collections.emptyMap());
        List<Company> companies = companyClient.getCompanies();
        // Creating map of companyId:company pairs
        Map<Long, Company> companyIdSelfMap = companies.stream()
                .collect(Collectors.toMap(Company::getId, Function.identity()));
        return positions.stream()
                .filter(position -> companyIdSelfMap.containsKey(position.getCompanyId())
                        && companyIdSelfMap.containsKey(position.getCreatedBy().getCompanyId()))
                .map(position -> {
                    Company positionCompany = companyIdSelfMap.get(position.getCompanyId());
                    Company userCompany = companyIdSelfMap.get(position.getCreatedBy().getCompanyId());
                    return positionMapper.toResponseDto(position, positionCompany, userCompany);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void createPosition(PositionRequestDto positionRequestDto) {
        log.debug("Creating new position from dto: {}", positionRequestDto);
        if (positionRequestDto.getItemVersion() == null) {
            log.error("Provided positionRequestDto didn't contain item's version");
            throw new NoVersionProvidedException(format(noItemVersionProvidedMessage,
                    positionRequestDto.getItemId()));
        }
        if (positionRequestDto.getCompanyId() != null) {
            // A request is sent to check if a company with such id exists
            // Feign decoder will check status and throw runtime exception if company not found
            companyClient.getCompanyById(positionRequestDto.getCompanyId());
        }
        Position position = positionMapper.toPosition(positionRequestDto);
        position.setCreated(LocalDateTime.now());
        log.debug("Mapping result: {}", position);
        positionDao.save(position);
    }

    @Override
    public void deletePosition(Long id) {
        log.debug("Deleting position by id: {}", id);
        positionDao.deleteById(id);
    }

    @Override
    public void updatePosition(Long id, PositionRequestDto updatedFields) {
        log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        Position position = positionDao.get(id);
        if (position.getVersion().equals(updatedFields.getVersion())) {
            log.debug("Position versions match");
            if (updatedFields.getCompanyId() != null) {
                // A request is sent to check if a company with such id exists
                // Feign decoder will check status and throw runtime exception if company not found
                companyClient.getCompanyById(updatedFields.getCompanyId());
            }
            if (updatedFields.getCreatedBy() != null) {
                User user = userDao.get(updatedFields.getCreatedBy());
                // A request is sent to check if a company with such id exists
                // Feign decoder will check status and throw runtime exception if company not found
                companyClient.getCompanyById(user.getCompanyId());
            }
            positionDao.update(id, positionMapper.toPosition(updatedFields));
        } else {
            log.error("Position versions do not match. Provided: {}, in the database: {}",
                    updatedFields.getVersion(), position.getVersion());
            throw new OptimisticLockException(positionVersionsMismatch);
        }
    }
}

package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionDao positionDao;
    private final PositionMapper positionMapper;

    @Value("${exception.message.positionVersionsMismatch}")
    private String positionVersionsMismatch;

    @Override
    public Position getPositionById(Long id) {
        log.debug("Fetching company by id: {}", id);
        return positionDao.get(id);
    }

    @Override
    public List<Position> getAllPositions(int pageNumber) {
        log.debug("Fetching all positions for page {}", pageNumber);
        return positionDao.getAll(pageNumber, Collections.emptyMap());
    }

    @Override
    public void createPosition(PositionDto positionDto) {
        log.debug("Creating new position from dto: {}", positionDto);
        Position position = positionMapper.toPosition(positionDto);
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
    public void updatePosition(Long id, PositionDto updatedFields) {
        log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        Position position = positionDao.get(id);
        if (position.getVersion().equals(updatedFields.getVersion())) {
            log.debug("Position versions match");
            positionDao.update(id, positionMapper.toPosition(updatedFields));
        } else {
            log.error("Position versions do not match. Provided: {}, in the database: {}",
                    updatedFields.getVersion(), position.getVersion());
            throw new OptimisticLockException(positionVersionsMismatch);
        }
    }
}

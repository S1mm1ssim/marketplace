package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Override
    public Position getPositionById(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching company by id: {}", id);
        }
        return positionDao.get(id);
    }

    @Override
    public List<Position> getAllPositions(int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all positions");
        }
        return positionDao.getAll(pageNumber);
    }

    @Override
    public void createPosition(PositionDto positionDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new position from dto: {}", positionDto);
        }
        Position position = positionMapper.toPosition(positionDto);
        position.setCreated(LocalDateTime.now());
        if (log.isDebugEnabled()) {
            log.debug("Mapping result: {}", position);
        }
        positionDao.save(position);
    }

    @Override
    public void deletePosition(Long id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting position by id: {}", id);
        }
        positionDao.deleteById(id);
    }

    @Override
    public void updatePosition(Long id, PositionDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating position with id: {}\nwith params: {}", id, updatedFields);
        }
        positionDao.update(id, positionMapper.toPosition(updatedFields));
    }
}

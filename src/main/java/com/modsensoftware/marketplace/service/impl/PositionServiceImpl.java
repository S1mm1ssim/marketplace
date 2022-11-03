package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.service.PositionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class PositionServiceImpl implements PositionService {

    private final PositionDao positionDao;
    private final PositionMapper positionMapper;

    @Override
    public Position getPositionById(Long id) throws EntityNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Fetching company by id: {}", id);
        }
        return positionDao.get(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Position> getAllPositions() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all positions");
        }
        return positionDao.getAll();
    }

    @Override
    public void createPosition(PositionDto positionDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new position: {}", positionDto);
        }
        positionDao.save(positionMapper.toPosition(positionDto));
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

package com.modsensoftware.marketplace.unit.position;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.service.impl.PositionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.OptimisticLockException;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class PositionServiceTest {

    @Mock
    private PositionDao positionDao;
    private final PositionMapper positionMapper = Mappers.getMapper(PositionMapper.class);

    private PositionServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new PositionServiceImpl(positionDao, positionMapper);
    }

    @Test
    public void canUpdateItem() {
        // given
        long id = 1L;
        long version = 1L;
        double amount = 10d;
        PositionDto updatedFields = new PositionDto();
        updatedFields.setVersion(version);
        updatedFields.setAmount(amount);
        Position position = new Position();
        position.setId(id);
        position.setVersion(version);
        BDDMockito.given(positionDao.get(id)).willReturn(position);

        // when
        underTest.updatePosition(id, updatedFields);

        // then
        BDDMockito.verify(positionDao).update(id, positionMapper.toPosition(updatedFields));
    }

    @Test
    public void shouldThrowExceptionWhenVersionsMismatch() {
        // given
        Long id = 1L;
        long version = 1L;
        long differentVersion = 2L;
        double amount = 10d;
        PositionDto updatedFields = new PositionDto();
        updatedFields.setVersion(version);
        updatedFields.setAmount(amount);
        Position position = new Position();
        position.setId(id);
        position.setVersion(differentVersion);
        BDDMockito.given(positionDao.get(id)).willReturn(position);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.updatePosition(id, updatedFields))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessage("Provided position version does not match with the one in the database");
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }
}

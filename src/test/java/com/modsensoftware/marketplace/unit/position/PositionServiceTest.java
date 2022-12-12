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
import org.springframework.test.util.ReflectionTestUtils;

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

    private static final String POSITION_VERSIONS_MISMATCH_MESSAGE
            = "Provided position version does not match with the one in the database";

    @BeforeEach
    void setUp() {
        underTest = new PositionServiceImpl(positionDao, positionMapper);
        ReflectionTestUtils.setField(underTest, "positionVersionsMismatch", POSITION_VERSIONS_MISMATCH_MESSAGE);
    }

    @Test
    public void canUpdatePosition() {
        // given
        long id = 1L;
        long version = 1L;
        double amount = 10d;
        PositionDto updatedFields = PositionDto.builder().amount(amount).version(version).build();
        Position position = Position.builder().version(version).build();
        BDDMockito.given(positionDao.get(id)).willReturn(position);

        // when
        underTest.updatePosition(id, updatedFields);

        // then
        BDDMockito.verify(positionDao).update(id, positionMapper.toPosition(updatedFields));
    }

    @Test
    public void shouldThrowExceptionWhenPositionVersionsMismatch() {
        // given
        Long id = 1L;
        long version = 1L;
        long differentVersion = 2L;
        double amount = 10d;
        PositionDto updatedFields = PositionDto.builder().amount(amount).version(version).build();
        Position position = Position.builder().version(differentVersion).build();
        BDDMockito.given(positionDao.get(id)).willReturn(position);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.updatePosition(id, updatedFields))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessage(POSITION_VERSIONS_MISMATCH_MESSAGE);
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }
}

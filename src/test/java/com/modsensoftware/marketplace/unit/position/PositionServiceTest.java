package com.modsensoftware.marketplace.unit.position;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.PositionDto;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.service.impl.PositionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.UUID;

import static java.lang.String.format;

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
    private static final String NO_ITEM_VERSION_PROVIDED_MESSAGE
            = "No version for item with id %s was provided";

    @BeforeEach
    void setUp() {
        underTest = new PositionServiceImpl(positionDao, positionMapper);
        ReflectionTestUtils.setField(underTest, "positionVersionsMismatch", POSITION_VERSIONS_MISMATCH_MESSAGE);
        ReflectionTestUtils.setField(underTest, "noItemVersionProvidedMessage", NO_ITEM_VERSION_PROVIDED_MESSAGE);
    }

    @Test
    public void canUpdatePosition() {
        // given
        long id = 1L;
        long version = 1L;
        BigDecimal amount = new BigDecimal("10");
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
        BigDecimal amount = new BigDecimal("10");
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

    @Test
    public void canSavePosition() {
        // given
        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        PositionDto toBeSaved = PositionDto.builder().itemId(UUID.randomUUID())
                .itemVersion(1L).companyId(2L).createdBy(UUID.randomUUID())
                .amount(amount).minAmount(minAmount).version(0L).build();

        // when
        underTest.createPosition(toBeSaved);

        // then
        ArgumentCaptor<Position> argumentCaptor = ArgumentCaptor.forClass(Position.class);
        BDDMockito.verify(positionDao).save(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("item.version", toBeSaved.getItemVersion())
                .hasFieldOrPropertyWithValue("item.id", toBeSaved.getItemId())
                .hasFieldOrPropertyWithValue("company.id", toBeSaved.getCompanyId())
                .hasFieldOrPropertyWithValue("createdBy.id", toBeSaved.getCreatedBy())
                .hasFieldOrPropertyWithValue("amount", amount.doubleValue())
                .hasFieldOrPropertyWithValue("minAmount", minAmount.doubleValue());
    }

    @Test
    @DisplayName("Should throw no version provided exception on save operation if item version is not provided")
    public void shouldThrowNoVersionProvidedExceptionOnSave() {
        // given
        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        PositionDto toBeSaved = PositionDto.builder().itemId(UUID.randomUUID())
                .itemVersion(null).companyId(2L).createdBy(UUID.randomUUID())
                .amount(amount).minAmount(minAmount).version(0L).build();
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.createPosition(toBeSaved))
                .isInstanceOf(NoVersionProvidedException.class)
                .hasMessage(format(NO_ITEM_VERSION_PROVIDED_MESSAGE, toBeSaved.getItemId()));
        BDDMockito.verify(positionDao, BDDMockito.never()).save(BDDMockito.any());
    }
}

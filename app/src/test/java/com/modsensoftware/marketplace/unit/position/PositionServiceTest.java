package com.modsensoftware.marketplace.unit.position;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.dto.request.PositionRequest;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.service.impl.CompanyClient;
import com.modsensoftware.marketplace.service.impl.PositionServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Mock
    private UserDao userDao;
    @Mock
    private CompanyClient companyClient;
    @Mock
    private PositionMapper positionMapper;

    private PositionServiceImpl underTest;

    private static final String POSITION_VERSIONS_MISMATCH_MESSAGE
            = "Provided position version does not match with the one in the database";
    private static final String NO_ITEM_VERSION_PROVIDED_MESSAGE
            = "No version for item with id %s was provided";

    @BeforeEach
    void setUp() {
        underTest = new PositionServiceImpl(positionDao, userDao, positionMapper, companyClient);
        ReflectionTestUtils.setField(underTest, "positionVersionsMismatch", POSITION_VERSIONS_MISMATCH_MESSAGE);
        ReflectionTestUtils.setField(underTest, "noItemVersionProvidedMessage", NO_ITEM_VERSION_PROVIDED_MESSAGE);
    }

    @Test
    public void canGetPositionById() {
        // given
        Long positionId = 1L;
        Long posCompanyId = 1L;
        Long userCompanyId = 2L;
        Company posCompany = Company.builder().id(posCompanyId).build();
        Company userCompany = Company.builder().id(userCompanyId).build();
        Position position = Position.builder()
                .companyId(posCompanyId)
                .createdBy(User.builder().companyId(userCompanyId).build())
                .build();
        BDDMockito.given(positionDao.get(positionId)).willReturn(position);
        BDDMockito.given(companyClient.getCompanyById(posCompanyId)).willReturn(posCompany);
        BDDMockito.given(companyClient.getCompanyById(userCompanyId)).willReturn(userCompany);

        // when
        underTest.getPositionById(positionId);

        // then
        BDDMockito.verify(positionMapper).toResponseDto(position, posCompany, userCompany);
    }

    @Test
    public void canUpdatePosition() {
        // given
        long id = 1L;
        long version = 1L;
        Long posCompanyId = 1L;
        Long userCompanyId = 2L;
        UUID userId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal(10);
        PositionRequest updatedFields = new PositionRequest(null, null, posCompanyId, userId, amount, null, version);
        Position position = Position.builder().id(id).version(version).build();
        BDDMockito.given(positionDao.get(id)).willReturn(position);
        BDDMockito.given(userDao.get(userId)).willReturn(User.builder().id(userId).companyId(userCompanyId).build());
        BDDMockito.given(companyClient.getCompanyById(posCompanyId)).willReturn(new Company());
        BDDMockito.given(companyClient.getCompanyById(userCompanyId)).willReturn(new Company());

        // when
        underTest.updatePosition(id, updatedFields);

        // then
        BDDMockito.verify(positionDao).update(id, positionMapper.toPosition(updatedFields));
    }

    @Test
    public void shouldThrowVersionMismatchExceptionWhenPositionVersionsMismatch() {
        // given
        Long id = 1L;
        long version = 1L;
        long differentVersion = 2L;
        BigDecimal amount = new BigDecimal(10);
        PositionRequest updatedFields = PositionRequest.builder().amount(amount).version(version).build();
        Position position = Position.builder().id(id).version(differentVersion).build();
        BDDMockito.given(positionDao.get(id)).willReturn(position);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.updatePosition(id, updatedFields))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessage(POSITION_VERSIONS_MISMATCH_MESSAGE);
        BDDMockito.verify(positionDao, BDDMockito.never()).update(BDDMockito.any(), BDDMockito.any());
    }

    @Test
    public void canDeletePosition() {
        // given
        Long positionId = 1L;
        // when
        underTest.deletePosition(positionId);
        // then
        BDDMockito.verify(positionDao).deleteById(positionId);
    }

    @Test
    public void canSavePosition() {
        // given
        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        Long companyId = 2L;
        PositionRequest toBeSaved = new PositionRequest(UUID.randomUUID(), 1L, companyId,
                UUID.randomUUID(), amount, minAmount, 0L);
        BDDMockito.given(companyClient.getCompanyById(companyId)).willReturn(new Company());
        BDDMockito.given(positionMapper.toPosition(toBeSaved)).willReturn(Position.builder()
                .item(Item.builder()
                        .id(toBeSaved.getItemId())
                        .version(toBeSaved.getItemVersion())
                        .build()
                ).companyId(companyId)
                .createdBy(User.builder().id(toBeSaved.getCreatedBy())
                        .build()
                ).amount(amount.doubleValue())
                .minAmount(minAmount.doubleValue())
                .version(0L)
                .build());

        // when
        underTest.createPosition(toBeSaved);

        // then
        ArgumentCaptor<Position> argumentCaptor = ArgumentCaptor.forClass(Position.class);
        BDDMockito.verify(positionDao).save(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("item.version", toBeSaved.getItemVersion())
                .hasFieldOrPropertyWithValue("item.id", toBeSaved.getItemId())
                .hasFieldOrPropertyWithValue("companyId", toBeSaved.getCompanyId())
                .hasFieldOrPropertyWithValue("createdBy.id", toBeSaved.getCreatedBy())
                .hasFieldOrPropertyWithValue("amount", amount.doubleValue())
                .hasFieldOrPropertyWithValue("minAmount", minAmount.doubleValue());
    }

    @Test
    @DisplayName("Should throw no item version provided exception on save operation if item version is not provided")
    public void shouldThrowNoVersionProvidedExceptionOnSave() {
        // given
        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        PositionRequest toBeSaved = new PositionRequest(UUID.randomUUID(), null, 2L,
                UUID.randomUUID(), amount, minAmount, 0L);
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.createPosition(toBeSaved))
                .isInstanceOf(NoVersionProvidedException.class)
                .hasMessage(format(NO_ITEM_VERSION_PROVIDED_MESSAGE, toBeSaved.getItemId()));
        BDDMockito.verify(positionDao, BDDMockito.never()).save(BDDMockito.any());
    }
}

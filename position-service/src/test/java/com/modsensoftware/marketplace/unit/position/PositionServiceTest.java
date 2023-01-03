package com.modsensoftware.marketplace.unit.position;

import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequestDto;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.exception.UnauthorizedOperationException;
import com.modsensoftware.marketplace.service.impl.CompanyClient;
import com.modsensoftware.marketplace.service.impl.PositionServiceImpl;
import com.modsensoftware.marketplace.service.impl.UserClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

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
    private CompanyClient companyClient;
    @Mock
    private UserClient userClient;
    @Mock
    private PositionMapper positionMapper;
    @Mock
    private Authentication authentication;

    private PositionServiceImpl underTest;

    private static final String NO_ITEM_VERSION_PROVIDED_MESSAGE
            = "No version for item with id %s was provided";

    @BeforeEach
    void setUp() {
        underTest = new PositionServiceImpl(positionDao, positionMapper, companyClient, userClient);
        ReflectionTestUtils.setField(underTest, "noItemVersionProvidedMessage", NO_ITEM_VERSION_PROVIDED_MESSAGE);
    }

    @Test
    public void canGetPositionById() {
        // given
        Long positionId = 1L;
        Long companyId = 1L;
        UUID userId = UUID.randomUUID();
        UserResponseDto user = UserResponseDto.builder()
                .id(userId)
                .username("username")
                .email("email@email.com")
                .company(Company.builder().id(companyId).build())
                .build();
        Position position = Position.builder()
                .companyId(companyId)
                .createdBy(userId)
                .build();
        BDDMockito.given(positionDao.get(positionId)).willReturn(position);
        BDDMockito.given(userClient.getUserById(userId)).willReturn(user);

        // when
        underTest.getPositionById(positionId);

        // then
        BDDMockito.verify(positionMapper).toResponseDto(position, user);
    }

    @Test
    public void canUpdatePosition() {
        // given
        UUID userId = UUID.randomUUID();
        BDDMockito.given(authentication.getName()).willReturn(userId.toString());
        long id = 1L;
        BigDecimal amount = new BigDecimal(10);
        UpdatePositionRequestDto updatedFields = new UpdatePositionRequestDto(amount, null);
        Position position = Position.builder()
                .createdBy(userId)
                .build();
        BDDMockito.given(positionDao.get(id)).willReturn(position);

        // when
        underTest.updatePosition(id, updatedFields, authentication);

        // then
        BDDMockito.verify(positionDao).update(id, positionMapper.toPosition(updatedFields));
    }

    @Test
    public void canDeletePosition() {
        // given
        UUID userId = UUID.randomUUID();
        BDDMockito.given(authentication.getName()).willReturn(userId.toString());
        Long positionId = 1L;
        BDDMockito.given(positionDao.get(positionId)).willReturn(Position.builder()
                .id(positionId)
                .createdBy(userId)
                .build());
        // when
        underTest.deletePosition(positionId, authentication);
        // then
        BDDMockito.verify(positionDao).deleteById(positionId);
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionIfPositionForDeleteNotFound() {
        // given
        Long positionId = 100000L;
        BDDMockito.given(positionDao.get(positionId)).willThrow(EntityNotFoundException.class);
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.deletePosition(positionId, authentication))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldThrowUnauthorizedOperationExceptionOnDeleteByAnotherUser() {
        // given
        BDDMockito.given(authentication.getName()).willReturn(UUID.randomUUID().toString());
        Long positionId = 1L;
        BDDMockito.given(positionDao.get(positionId)).willReturn(Position.builder()
                .id(positionId)
                .createdBy(UUID.randomUUID())
                .build());
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.deletePosition(positionId, authentication))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    public void canSavePosition() {
        // given
        UUID userId = UUID.randomUUID();
        Long companyId = 2L;
        UserResponseDto user = UserResponseDto.builder()
                .id(userId)
                .company(Company.builder().id(companyId).build())
                .build();
        BDDMockito.given(authentication.getName()).willReturn(userId.toString());

        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        CreatePositionRequestDto toBeSaved = new CreatePositionRequestDto(UUID.randomUUID(), 1L, amount, minAmount);
        BDDMockito.given(userClient.getUserById(userId)).willReturn(user);
        BDDMockito.given(positionMapper.toPosition(toBeSaved, user)).willReturn(Position.builder()
                .item(Item.builder()
                        .id(toBeSaved.getItemId())
                        .version(toBeSaved.getItemVersion())
                        .build())
                .companyId(companyId)
                .createdBy(user.getId())
                .amount(amount.doubleValue())
                .minAmount(minAmount.doubleValue())
                .build());

        // when
        underTest.createPosition(toBeSaved, authentication);

        // then
        ArgumentCaptor<Position> argumentCaptor = ArgumentCaptor.forClass(Position.class);
        BDDMockito.verify(positionDao).save(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("item.version", toBeSaved.getItemVersion())
                .hasFieldOrPropertyWithValue("item.id", toBeSaved.getItemId())
                .hasFieldOrPropertyWithValue("companyId", companyId)
                .hasFieldOrPropertyWithValue("createdBy", userId)
                .hasFieldOrPropertyWithValue("amount", amount.doubleValue())
                .hasFieldOrPropertyWithValue("minAmount", minAmount.doubleValue());
    }

    @Test
    @DisplayName("Should throw no item version provided exception on save operation if item version is not provided")
    public void shouldThrowNoVersionProvidedExceptionOnSave() {
        // given
        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        CreatePositionRequestDto toBeSaved = new CreatePositionRequestDto(UUID.randomUUID(), null, amount, minAmount);
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.createPosition(toBeSaved, authentication))
                .isInstanceOf(NoVersionProvidedException.class)
                .hasMessage(format(NO_ITEM_VERSION_PROVIDED_MESSAGE, toBeSaved.getItemId()));
        BDDMockito.verify(positionDao, BDDMockito.never()).save(BDDMockito.any());
    }
}

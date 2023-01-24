package com.modsensoftware.marketplace.unit.position;

import com.modsensoftware.marketplace.dao.ItemDao;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.mapper.PositionMapper;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequest;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.NoVersionProvidedException;
import com.modsensoftware.marketplace.exception.UnauthorizedOperationException;
import com.modsensoftware.marketplace.service.impl.PositionServiceImpl;
import com.modsensoftware.marketplace.service.impl.UserClient;
import com.mongodb.client.result.DeleteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class PositionServiceTest {

    @Mock
    private ItemDao itemDao;
    @Mock
    private PositionDao positionDao;
    @Mock
    private UserClient userClient;
    @Mock
    private Authentication authentication;

    private PositionServiceImpl underTest;

    private static final PositionMapper POSITION_MAPPER = new PositionMapper();

    private static final String NO_ITEM_VERSION_PROVIDED_MESSAGE
            = "No version for item with id %s was provided";

    @BeforeEach
    void setUp() {
        underTest = new PositionServiceImpl(positionDao, itemDao, POSITION_MAPPER, userClient);
        ReflectionTestUtils.setField(underTest, "noItemVersionProvidedMessage", NO_ITEM_VERSION_PROVIDED_MESSAGE);
    }

    @Test
    public void canGetPositionById() {
        // given
        String positionId = "12345";
        Long companyId = 1L;
        String userId = UUID.randomUUID().toString();
        UserResponse user = UserResponse.builder()
                .id(userId)
                .username("username")
                .email("email@email.com")
                .company(Company.builder().id(companyId).build())
                .build();
        Position position = Position.builder()
                .companyId(companyId)
                .createdBy(userId)
                .build();
        BDDMockito.given(positionDao.get(positionId)).willReturn(Mono.just(position));
        BDDMockito.given(userClient.getUserById(userId)).willReturn(Mono.just(user));

        // when
        Mono<PositionResponseDto> positionById = underTest.getPositionById(positionId);

        // then
        positionById
                .as(StepVerifier::create)
                .expectNext(POSITION_MAPPER.toResponseDto(position, user))
                .verifyComplete();
    }

    @Test
    public void canUpdatePosition() {
        // given
        String userId = UUID.randomUUID().toString();
        BDDMockito.given(authentication.getName()).willReturn(userId);
        String id = "12345";
        BigDecimal amount = new BigDecimal(10);
        UpdatePositionRequest updatedFields = new UpdatePositionRequest(amount, null);
        Position position = Position.builder()
                .createdBy(userId)
                .build();
        Position expected = Position.builder()
                .createdBy(userId)
                .amount(amount.doubleValue())
                .build();
        BDDMockito.given(positionDao.get(id)).willReturn(Mono.just(position));
        BDDMockito.given(positionDao.update(id, POSITION_MAPPER.toPosition(updatedFields)))
                .willReturn(Mono.just(expected));

        // when
        Mono<Position> updated = underTest.updatePosition(id, updatedFields, authentication);

        // then
        updated.as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    public void canDeletePosition() {
        // given
        String userId = UUID.randomUUID().toString();
        BDDMockito.given(authentication.getName()).willReturn(userId);
        String positionId = "12345";
        BDDMockito.given(positionDao.get(positionId)).willReturn(
                Mono.just(Position.builder()
                        .id(positionId)
                        .createdBy(userId)
                        .build()));
        BDDMockito.given(positionDao.deleteById(positionId)).willReturn(Mono.just(DeleteResult.acknowledged(1)));
        // when
        Mono<DeleteResult> deleteResultMono = underTest.deletePosition(positionId, authentication);
        // then
        deleteResultMono.as(StepVerifier::create)
                .expectNext(DeleteResult.acknowledged(1))
                .verifyComplete();
    }

    @Test
    public void shouldThrowEntityNotFoundExceptionIfPositionForDeleteNotFound() {
        // given
        String positionId = "100000";
        BDDMockito.given(positionDao.get(positionId)).willReturn(Mono.error(new EntityNotFoundException()));
        // when
        // then
        underTest.deletePosition(positionId, authentication)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable.getClass().equals(EntityNotFoundException.class))
                .verify();
    }

    @Test
    public void shouldThrowUnauthorizedOperationExceptionOnDeleteByAnotherUser() {
        // given
        BDDMockito.given(authentication.getName()).willReturn(UUID.randomUUID().toString());
        String positionId = "12345";
        BDDMockito.given(positionDao.get(positionId)).willReturn(
                Mono.just(Position.builder()
                        .id(positionId)
                        .createdBy(UUID.randomUUID().toString())
                        .build()));
        // when
        Mono<DeleteResult> deleteResultMono = underTest.deletePosition(positionId, authentication);
        // then
        deleteResultMono.as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable.getClass().equals(UnauthorizedOperationException.class))
                .verify();
    }

    @Test
    public void canSavePosition() {
        // given
        String userId = UUID.randomUUID().toString();
        Long companyId = 2L;
        UserResponse user = UserResponse.builder()
                .id(userId)
                .company(Company.builder().id(companyId).build())
                .build();
        BDDMockito.given(authentication.getName()).willReturn(userId);

        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        CreatePositionRequest toBeSaved = new CreatePositionRequest(UUID.randomUUID().toString(), 1L, amount, minAmount);
        Item item = Item.builder().id(toBeSaved.getItemId()).version(toBeSaved.getItemVersion()).build();
        Position expected = Position.builder()
                .id("12345")
                .item(item)
                .companyId(companyId)
                .createdBy(user.getId())
                .amount(amount.doubleValue())
                .minAmount(minAmount.doubleValue())
                .build();
        BDDMockito.given(itemDao.get(item.getId())).willReturn(Mono.just(item));
        BDDMockito.given(userClient.getUserById(userId)).willReturn(Mono.just(user));
        BDDMockito.given(positionDao.save(BDDMockito.any())).willReturn(Mono.just(expected));

        // when
        Mono<Position> actual = underTest.createPosition(toBeSaved, authentication);

        // then
        actual.as(StepVerifier::create)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw no item version provided exception on save operation if item version is not provided")
    public void shouldThrowNoVersionProvidedExceptionOnSave() {
        // given
        BigDecimal amount = new BigDecimal(10);
        BigDecimal minAmount = new BigDecimal(1);
        CreatePositionRequest toBeSaved = new CreatePositionRequest(
                UUID.randomUUID().toString(), null, amount, minAmount);
        // when
        Mono<Position> positionMono = underTest.createPosition(toBeSaved, authentication);
        // then
        positionMono.as(StepVerifier::create)
                .expectErrorMatches(throwable -> throwable.getClass().equals(NoVersionProvidedException.class)
                        && throwable.getMessage().equals(format(NO_ITEM_VERSION_PROVIDED_MESSAGE, toBeSaved.getItemId())))
                .verify();
    }
}

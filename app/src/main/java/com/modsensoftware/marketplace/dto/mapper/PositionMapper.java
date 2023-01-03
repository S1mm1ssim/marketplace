package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequestDto;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Component
public class PositionMapper {

    private final UserMapper userMapper;

    public Position toPosition(CreatePositionRequestDto requestDto, UUID userId) {
        Item item = Item.builder()
                .id(requestDto.getItemId())
                .version(requestDto.getItemVersion())
                .build();
        Double minAmount = null;
        if (requestDto.getMinAmount() != null) {
            minAmount = requestDto.getMinAmount().doubleValue();
        }
        Double amount = null;
        if (requestDto.getAmount() != null) {
            amount = requestDto.getAmount().doubleValue();
        }
        return Position.builder()
                .item(item)
                .companyId(requestDto.getCompanyId())
                .createdBy(User.builder().id(userId).build())
                .amount(amount)
                .minAmount(minAmount)
                .build();
    }

    public Position toPosition(UpdatePositionRequestDto requestDto) {
        Double minAmount = null;
        if (requestDto.getMinAmount() != null) {
            minAmount = requestDto.getMinAmount().doubleValue();
        }
        Double amount = null;
        if (requestDto.getAmount() != null) {
            amount = requestDto.getAmount().doubleValue();
        }
        return Position.builder()
                .amount(amount)
                .minAmount(minAmount)
                .build();
    }


    public PositionResponseDto toResponseDto(Position position, Company positionCompany, Company userCompany) {
        return new PositionResponseDto(
                position.getId(),
                position.getItem(),
                positionCompany,
                userMapper.toResponseDto(position.getCreatedBy(), userCompany),
                position.getCreated(),
                position.getAmount(),
                position.getMinAmount()
        );
    }
}

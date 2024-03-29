package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.request.CreatePositionRequest;
import com.modsensoftware.marketplace.dto.request.UpdatePositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Component
public class PositionMapper {

    public Position toPosition(CreatePositionRequest requestDto, UserResponse user) {
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
                .companyId(user.getCompany().getId())
                .createdBy(user.getId())
                .amount(amount)
                .minAmount(minAmount)
                .build();
    }

    public Position toPosition(UpdatePositionRequest requestDto) {
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


    public PositionResponse toResponseDto(Position position, UserResponse user) {
        return new PositionResponse(
                position.getId(),
                position.getItem(),
                user.getCompany(),
                user,
                position.getCreated(),
                position.getAmount(),
                position.getMinAmount()
        );
    }
}

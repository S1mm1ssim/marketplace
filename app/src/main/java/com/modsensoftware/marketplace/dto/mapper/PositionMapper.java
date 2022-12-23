package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.request.PositionRequestDto;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Component
public class PositionMapper {

    private final UserMapper userMapper;

    public Position toPosition(PositionRequestDto requestDto) {
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
                .createdBy(User.builder().id(requestDto.getCreatedBy()).build())
                .amount(amount)
                .minAmount(minAmount)
                .version(requestDto.getVersion())
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
                position.getMinAmount(),
                position.getVersion()
        );
    }
}

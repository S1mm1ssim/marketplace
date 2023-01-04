package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.dto.request.PositionRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Component
public class PositionMapper {

    public PositionRequest toPositionRequestDto(PositionResponse position) {
        return new PositionRequest(
                position.getItem().getId(),
                position.getItem().getVersion(),
                position.getCompany().getId(),
                position.getCreatedBy().getId(),
                BigDecimal.valueOf(position.getAmount()),
                BigDecimal.valueOf(position.getMinAmount()),
                position.getVersion()
        );
    }
}

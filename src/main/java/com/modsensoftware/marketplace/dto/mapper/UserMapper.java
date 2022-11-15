package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mappings({
            @Mapping(target = "company.id", source = "userDto.companyId")
    })
    User toUser(UserDto userDto);
}

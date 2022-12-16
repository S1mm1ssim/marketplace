package com.modsensoftware.marketplace.dto.mapper;

import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.request.UserRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@RequiredArgsConstructor
@Component
public class UserMapper {

    private final CompanyMapper companyMapper;

    public UserRepresentation toKeycloakUserRepresentation(UserRequestDto userDto) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(userDto.getUsername());
        userRepresentation.setEmail(userDto.getEmail());
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(userDto.getPassword());
        userRepresentation.setCredentials(Collections.singletonList(passwordCred));
        return userRepresentation;
    }

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getCreated(),
                user.getUpdated(),
                companyMapper.toCompanyDto(user.getCompany())
        );
    }

    public User toUser(UserRequestDto requestDto) {
        Company company = new Company();
        company.setId(requestDto.getCompanyId());
        return User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .company(company)
                .build();
    }
}

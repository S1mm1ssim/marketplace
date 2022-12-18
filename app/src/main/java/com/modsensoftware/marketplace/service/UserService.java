package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.UserRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface UserService {

    UserResponseDto getUserById(UUID id);

    List<UserResponseDto> getAllUsers(int pageNumber, String email,
                                      String name, String createdBetween, Long companyId);

    String createUser(UserRequestDto userDto);

    void deleteUser(UUID id);

    void updateUser(UUID id, UserRequestDto updatedFields);
}

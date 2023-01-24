package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.UserRequest;
import com.modsensoftware.marketplace.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface UserService {

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers(int pageNumber, String email,
                                   String name, String createdBetween, Long companyId);

    String createUser(UserRequest userDto);

    void deleteUser(UUID id);

    void updateUser(UUID id, UserRequest updatedFields);
}

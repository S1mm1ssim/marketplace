package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
public interface UserService {

    User getUserById(UUID id);

    List<User> getAllUsers();

    void createUser(UserDto userDto);

    void deleteUser(UUID id);

    void updateUser(UUID id, UserDto updatedFields);
}

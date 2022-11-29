package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;
import com.modsensoftware.marketplace.dto.mapper.UserMapper;
import com.modsensoftware.marketplace.enums.Role;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.service.UserService;
import com.modsensoftware.marketplace.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.modsensoftware.marketplace.constants.Constants.*;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    private static final String ENTITY_ALREADY_EXISTS_EXCEPTION
            = "User with email %s already exists";

    @Value("${default.role}")
    private String defaultRole;

    @Override
    public User getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        return userDao.get(id);
    }

    @Override
    public List<User> getAllUsers(int pageNumber, String email,
                                  String name, String createdBetween,
                                  Long companyId) {
        log.debug("Fetching all users for page {}. "
                        + "Filter by email: {}, name: {}, created between: {}, company id: {}",
                pageNumber, email, name, createdBetween, companyId);
        Map<String, String> filterProperties = new HashMap<>();
        Utils.putIfNotNull(EMAIL_FILTER_NAME, email, filterProperties::put);
        Utils.putIfNotNull(NAME_FILTER_NAME, name, filterProperties::put);
        if (createdBetween != null) {
            filterProperties.put(CREATED_BETWEEN_FILTER_NAME, createdBetween);
        }
        if (companyId != null) {
            filterProperties.put(COMPANY_ID_FILTER_NAME, companyId.toString());
        }
        return userDao.getAll(pageNumber, filterProperties);
    }

    @Override
    public void createUser(UserDto userDto) {
        log.debug("Creating new user from dto: {}", userDto);
        if (!userDao.existsByEmail(userDto.getEmail())) {
            User user = userMapper.toUser(userDto);
            user.setRole(Role.valueOf(defaultRole));
            user.setCreated(LocalDateTime.now());
            user.setUpdated(LocalDateTime.now());
            log.debug("Mapping result: {}", user);
            userDao.save(user);
        } else {
            throw new EntityAlreadyExistsException(
                    format(ENTITY_ALREADY_EXISTS_EXCEPTION, userDto.getEmail())
            );
        }
    }

    @Override
    public void deleteUser(UUID id) {
        log.debug("Deleting user by id: {}", id);
        userDao.deleteById(id);
    }

    @Override
    public void updateUser(UUID id, UserDto updatedFields) {
        log.debug("Updating user with id: {}\nwith params: {}", id, updatedFields);
        User user = userMapper.toUser(updatedFields);
        user.setUpdated(LocalDateTime.now());
        userDao.update(id, user);
    }
}

package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;
import com.modsensoftware.marketplace.dto.mapper.UserMapper;
import com.modsensoftware.marketplace.enums.Role;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    private static final Role DEFAULT_ROLE = Role.MANAGER;

    @Override
    public User getUserById(UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching user by id: {}", id);
        }
        return userDao.get(id);
    }

    @Override
    public List<User> getAllUsers(int pageNumber) {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all users");
        }
        return userDao.getAll(pageNumber);
    }

    @Override
    public void createUser(UserDto userDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new user: {}", userDto);
        }
        if (!userDao.existsByEmail(userDto.getEmail())) {
            User user = userMapper.toUser(userDto);
            user.setRole(DEFAULT_ROLE);
            user.setCreated(LocalDateTime.now());
            user.setUpdated(LocalDateTime.now());
            userDao.save(user);
        } else {
            throw new EntityAlreadyExistsException(format("User with email %s already exists",
                    userDto.getEmail()));
        }
    }

    @Override
    public void deleteUser(UUID id) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting user by id: {}", id);
        }
        userDao.deleteById(id);
    }

    @Override
    public void updateUser(UUID id, UserDto updatedFields) {
        if (log.isDebugEnabled()) {
            log.debug("Updating user with id: {}\nwith params: {}", id, updatedFields);
        }
        User user = userMapper.toUser(updatedFields);
        user.setUpdated(LocalDateTime.now());
        userDao.update(id, user);
    }
}

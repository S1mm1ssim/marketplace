package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;
import com.modsensoftware.marketplace.dto.mapper.UserMapper;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    @Override
    public User getUserById(UUID id) throws EntityNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Fetching user by id: {}", id);
        }
        return userDao.get(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<User> getAllUsers() {
        if (log.isDebugEnabled()) {
            log.debug("Fetching all users");
        }
        return userDao.getAll();
    }

    @Override
    public void createUser(UserDto userDto) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new user: {}", userDto);
        }
        userDao.save(userMapper.toUser(userDto));
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
        userDao.update(id, userMapper.toUser(updatedFields));
    }
}

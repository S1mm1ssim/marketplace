package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.mapper.UserMapper;
import com.modsensoftware.marketplace.dto.request.UserRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.exception.PasswordAbsenceException;
import com.modsensoftware.marketplace.service.UserService;
import com.modsensoftware.marketplace.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.modsensoftware.marketplace.constants.Constants.COMPANY_ID_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.CREATED_BETWEEN_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.NAME_FILTER_NAME;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;
    private final UserDao userDao;
    private final UserMapper userMapper;

    @Value("${idm.realm-name}")
    private String realmName;
    @Value("${exception.message.nonUniqueUser}")
    private String nonUniqueUserExceptionMessage;
    @Value("${exception.message.userNotFound}")
    private String userNotFoundMessage;
    @Value("${exception.message.userPasswordAbsent}")
    private String userPasswordAbsentMessage;
    @Value("${default.role}")
    private String defaultRole;
    @Value("${default.page.size}")
    private int pageSize;

    @Override
    public UserResponseDto getUserById(UUID id) {
        log.debug("Fetching user by id: {}", id);
        return userMapper.toResponseDto(userDao.get(id));

    }

    @Override
    public List<UserResponseDto> getAllUsers(int pageNumber, String email,
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
        List<User> users = userDao.getAll(pageNumber, filterProperties);
        return users.stream().map(userMapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public String createUser(UserRequestDto userDto) {
        log.debug("Registering new user from dto: {}", userDto);
        UserRepresentation userRepresentation = userMapper.toKeycloakUserRepresentation(userDto);
        RealmResource realmResource = keycloak.realm(realmName);
        UsersResource usersResource = realmResource.users();
        String userId;
        try {
            // Creating user in keycloak
            Response response = usersResource.create(userRepresentation);
            userId = CreatedResponseUtil.getCreatedId(response);
        } catch (WebApplicationException ex) {
            log.error("Could not save user in keycloak. Exception message {}", ex.getMessage());
            throw new EntityAlreadyExistsException(nonUniqueUserExceptionMessage);
        }
        // Adding default application role to keycloak user
        UserResource userResource = usersResource.get(userId);
        RoleRepresentation managerRealmRole = realmResource.roles().get(defaultRole).toRepresentation();
        userResource.roles().realmLevel().add(List.of(managerRealmRole));

        // Creating local user
        User user = userMapper.toUser(userDto);
        user.setId(UUID.fromString(userId));
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        userDao.save(user);
        return userId;
    }

    @Override
    public void deleteUser(UUID id) {
        log.debug("Deleting user by id: {}", id);
        UsersResource usersResource = getUsersResource();
        try {
            usersResource.get(String.valueOf(id)).remove();
        } catch (NotFoundException e) {
            log.error("User with id {} not found in keycloak", id);
            throw new EntityNotFoundException(format(userNotFoundMessage, id));
        }
        userDao.deleteById(id);
    }

    @Override
    public void updateUser(UUID id, UserRequestDto updatedFields) {
        if (updatedFields.getPassword() == null) {
            log.error("Could not update user. Password is not provided");
            throw new PasswordAbsenceException(userPasswordAbsentMessage);
        }
        log.debug("Updating user with id: {}\nwith params: {}", id, updatedFields);
        UsersResource usersResource = getUsersResource();
        UserRepresentation userRepresentation = userMapper.toKeycloakUserRepresentation(updatedFields);
        try {
            usersResource.get(String.valueOf(id)).update(userRepresentation);
        } catch (NotFoundException e) {
            log.error("User with id {} not found in keycloak", id);
            throw new EntityNotFoundException(format(userNotFoundMessage, id));
        }
        User user = userMapper.toUser(updatedFields);
        user.setId(id);
        user.setUpdated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        userDao.update(id, user);
    }

    private UsersResource getUsersResource() {
        RealmResource realmResource = keycloak.realm(realmName);
        return realmResource.users();
    }
}

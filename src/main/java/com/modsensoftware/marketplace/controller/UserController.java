package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.constants.Constants;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;
import com.modsensoftware.marketplace.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(produces = {"application/json"})
    public List<User> getAllUsers(
            @RequestParam(name = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "created", required = false) String createdBetween,
            @RequestParam(name = "companyId", required = false) Long companyId
    ) {
        log.debug("Fetching all users");
        return userService.getAllUsers(pageNumber, email, name, createdBetween, companyId);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public User getUserById(@PathVariable(name = "id") UUID id) {
        log.debug("Fetching user by id={}", id);
        return userService.getUserById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createUser(@RequestBody UserDto userDto) {
        log.debug("Creating new user from dto: {}", userDto);
        userService.createUser(userDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        log.debug("Deleting user by id: {}", id);
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable(name = "id") UUID id, @RequestBody UserDto updatedFields) {
        log.debug("Updating user with id: {}\nwith params: {}", id, updatedFields);
        userService.updateUser(id, updatedFields);
    }
}

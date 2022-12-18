package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.dto.request.UserRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
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

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.modsensoftware.marketplace.constants.Constants.COMPANY_ID_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.CREATED_BETWEEN_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.DEFAULT_PAGE_NUMBER;
import static com.modsensoftware.marketplace.constants.Constants.EMAIL_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.NAME_FILTER_NAME;
import static com.modsensoftware.marketplace.constants.Constants.PAGE_FILTER_NAME;

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
    public List<UserResponseDto> getAllUsers(
            @RequestParam(name = PAGE_FILTER_NAME, defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(name = EMAIL_FILTER_NAME, required = false) String email,
            @RequestParam(name = NAME_FILTER_NAME, required = false) String name,
            @RequestParam(name = CREATED_BETWEEN_FILTER_NAME, required = false) String createdBetween,
            @RequestParam(name = COMPANY_ID_FILTER_NAME, required = false) Long companyId
    ) {
        log.debug("Fetching all users for page {}. "
                        + "Filter by email: {}, name: {}, created between: {}, company id: {}",
                pageNumber, email, name, createdBetween, companyId);
        return userService.getAllUsers(pageNumber, email, name, createdBetween, companyId);
    }

    @GetMapping(value = "/{id}", produces = {"application/json"})
    public UserResponseDto getUserById(@PathVariable(name = "id") UUID id) {
        log.debug("Fetching user by id: {}", id);
        return userService.getUserById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String createUser(@Valid @RequestBody UserRequestDto userDto) {
        log.debug("Creating new user from dto: {}", userDto);
        return userService.createUser(userDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        log.debug("Deleting user by id: {}", id);
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable(name = "id") UUID id,
                           @Valid @RequestBody UserRequestDto updatedFields) {
        log.debug("Updating user with id: {}\nwith params: {}", id, updatedFields);
        userService.updateUser(id, updatedFields);
    }
}
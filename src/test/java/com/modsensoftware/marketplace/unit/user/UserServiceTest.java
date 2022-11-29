package com.modsensoftware.marketplace.unit.user;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.UserDto;
import com.modsensoftware.marketplace.dto.mapper.UserMapper;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.service.impl.UserServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private UserServiceImpl underTest;

    private static final String USER_EMAIL_TAKEN_MESSAGE = "User with email %s already exists";

    @BeforeEach
    void setUp() {
        underTest = new UserServiceImpl(userDao, userMapper);
        ReflectionTestUtils.setField(underTest, "defaultRole", "MANAGER");
        ReflectionTestUtils.setField(underTest, "userEmailTakenMessage", USER_EMAIL_TAKEN_MESSAGE);
    }

    @Test
    public void canGetAllUsers() {
        // given
        int pageNumber = 0;
        String emailFilterValue = "email";
        String nameFilterValue = "name";
        String createdBetween = "2022-11-04T12:00:00,2022-11-18T12:00:00";
        Long companyId = 4L;
        Map<String, String> filterProps = new HashMap<>();
        filterProps.put("email", emailFilterValue);
        filterProps.put("name", nameFilterValue);
        filterProps.put("created", createdBetween);
        filterProps.put("companyId", companyId.toString());
        // when
        underTest.getAllUsers(pageNumber, emailFilterValue,
                nameFilterValue, createdBetween, companyId);
        // then
        BDDMockito.verify(userDao).getAll(pageNumber, filterProps);
    }

    @Test
    public void canCreateUser() {
        // given
        String email = "Email";
        UserDto dto = new UserDto("username", email, "name", 1L);
        BDDMockito.given(userDao.existsByEmail(email)).willReturn(false);

        // when
        underTest.createUser(dto);
        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        BDDMockito.verify(userDao).save(userCaptor.capture());
        Assertions.assertThat(userCaptor.getValue())
                .hasFieldOrPropertyWithValue("name", dto.getName())
                .hasFieldOrPropertyWithValue("email", dto.getEmail())
                .hasFieldOrPropertyWithValue("username", dto.getUsername())
                .hasFieldOrPropertyWithValue("company.id", dto.getCompanyId());
    }

    @Test
    public void saveShouldThrowEntityAlreadyExistsException() {
        // given
        String email = "Email";
        UserDto dto = new UserDto("username", email, "name", 1L);
        BDDMockito.given(userDao.existsByEmail(email)).willReturn(true);
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.createUser(dto))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage(String.format(USER_EMAIL_TAKEN_MESSAGE, dto.getEmail()));
        BDDMockito.verify(userDao, BDDMockito.never()).save(BDDMockito.any());
    }
}

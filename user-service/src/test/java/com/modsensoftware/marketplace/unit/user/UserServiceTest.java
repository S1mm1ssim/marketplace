package com.modsensoftware.marketplace.unit.user;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.mapper.UserMapper;
import com.modsensoftware.marketplace.service.impl.CompanyClient;
import com.modsensoftware.marketplace.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CompanyClient companyClient;

    private static final String USER_NOT_FOUND_MESSAGE = "User entity with uuid=%s is not found.";

    private UserServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new UserServiceImpl(null, userDao, userMapper, companyClient);
        ReflectionTestUtils.setField(underTest, "userNotFoundMessage", USER_NOT_FOUND_MESSAGE);
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
    public void canGetUserById() {
        // given

        UUID id = UUID.randomUUID();
        Long companyId = 1L;
        User user = User.builder().id(id).companyId(companyId).build();
        Company company = Company.builder().id(companyId).build();
        BDDMockito.given(userDao.get(id)).willReturn(user);
        BDDMockito.given(companyClient.getCompanyById(companyId)).willReturn(company);
        // when
        underTest.getUserById(id);
        // then
        BDDMockito.verify(userMapper).toResponseDto(user, company);
    }
}

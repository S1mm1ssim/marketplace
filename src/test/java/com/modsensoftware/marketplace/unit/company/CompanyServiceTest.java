package com.modsensoftware.marketplace.unit.company;

import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.dto.CompanyDto;
import com.modsensoftware.marketplace.dto.mapper.CompanyMapper;
import com.modsensoftware.marketplace.exception.EntityAlreadyExistsException;
import com.modsensoftware.marketplace.service.impl.CompanyServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/18/2022
 */
@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock
    private CompanyDao companyDao;

    private final CompanyMapper companyMapper = Mappers.getMapper(CompanyMapper.class);

    private CompanyServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new CompanyServiceImpl(companyDao, companyMapper);
    }

    @Test
    public void canGetAllCompanies() {
        // given
        int pageNumber = 0;
        String emailFilterValue = "email";
        String nameFilterValue = "name";
        Map<String, String> filterProps = new HashMap<>();
        filterProps.put("email", emailFilterValue);
        filterProps.put("name", nameFilterValue);
        // when
        underTest.getAllCompanies(pageNumber, emailFilterValue, nameFilterValue);

        // then
        BDDMockito.verify(companyDao).getAll(pageNumber, filterProps);
    }

    @Test
    public void canCreateCompany() {
        // given
        CompanyDto dto = new CompanyDto("Name", "Email", "Description");
        BDDMockito.given(companyDao.existsByEmail(Mockito.any())).willReturn(false);

        // when
        underTest.createCompany(dto);
        // then
        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        BDDMockito.verify(companyDao).save(companyCaptor.capture());
        Assertions.assertThat(companyCaptor.getValue())
                .hasFieldOrPropertyWithValue("name", dto.getName())
                .hasFieldOrPropertyWithValue("email", dto.getEmail())
                .hasFieldOrPropertyWithValue("description", dto.getDescription());
    }

    @Test
    public void saveShouldThrowEntityAlreadyExistsException() {
        // given
        String email = "Email";
        CompanyDto dto = new CompanyDto("Name", email, "Description");
        BDDMockito.given(companyDao.existsByEmail(email)).willReturn(true);
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.createCompany(dto))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage(String.format("Company with email %s already exists", dto.getEmail()));
        BDDMockito.verify(companyDao, BDDMockito.never()).save(BDDMockito.any());
    }
}

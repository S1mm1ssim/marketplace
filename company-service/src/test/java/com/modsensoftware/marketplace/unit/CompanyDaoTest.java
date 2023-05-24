package com.modsensoftware.marketplace.unit;

import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CompanyDaoTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CompanyDao underTest;

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();
    @Container
    public static RedisContainer redisContainer = RedisContainer.getInstance();

    @Value("${default.page.size}")
    private int pageSize;
    @Value("${exception.message.companyNotFound}")
    private String companyNotFoundMessage;

    @Test
    public void canSaveCompany() {
        // given
        Company company = generateDefaultCompanyWithIsSoftDeleted(false);

        // when
        underTest.save(company);

        // then
        Company saved = sessionFactory.openSession().get(Company.class, company.getId());
        Assertions.assertThat(saved).isEqualTo(company);

        // clean up
        deleteCompany(company);
    }

    @Test
    public void canSoftDeleteById() {
        // given
        Company company = generateDefaultCompanyWithIsSoftDeleted(false);
        underTest.save(company);

        // when
        underTest.deleteById(company.getId());

        // then
        Company shouldBeSoftDeleted = sessionFactory.openSession().get(Company.class, company.getId());
        Assertions.assertThat(shouldBeSoftDeleted.getIsDeleted()).isTrue();

        // clean up
        deleteCompany(company);
    }

    @Test
    public void canGetById() {
        // given
        Company company = generateDefaultCompanyWithIsSoftDeleted(false);
        underTest.save(company);

        // when
        Company saved = underTest.get(company.getId());

        // then
        Assertions.assertThat(saved).isEqualTo(company);

        // clean up
        deleteCompany(company);
    }

    @Test
    public void getByIdShouldThrowEntityNotFoundExceptionIfSoftDeleted() {
        // given
        Company company = generateDefaultCompanyWithIsSoftDeleted(true);
        underTest.save(company);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(company.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format(companyNotFoundMessage, company.getId()));

        // clean up
        deleteCompany(company);
    }

    @Test
    public void shouldExistByEmail() {
        // given
        Company company = generateDefaultCompanyWithIsSoftDeleted(false);
        underTest.save(company);

        // when
        boolean existsByEmail = underTest.existsByEmail(company.getEmail());

        // then
        Assertions.assertThat(existsByEmail).isTrue();

        // clean up
        deleteCompany(company);
    }

    @CsvSource({
            "true, ",
            "false, sdshdagsjdbhauysdhbjaskdbakjshj",
    })
    @ParameterizedTest
    public void companyShouldNotExistByEmail(boolean isSoftDeleted, String appendValueToGuaranteeEmailNotFound) {
        // given
        Company company = generateDefaultCompanyWithIsSoftDeleted(isSoftDeleted);
        underTest.save(company);

        // when
        boolean existsByEmail = underTest.existsByEmail(company.getEmail() + appendValueToGuaranteeEmailNotFound);

        // then
        Assertions.assertThat(existsByEmail).isFalse();

        // clean up
        deleteCompany(company);
    }

    @Test
    public void canGetAllNonSoftDeletedCompanies() {
        // given
        List<Company> companies = new ArrayList<>();
        Company company;
        Company softDeleted;
        Random random = new Random();
        for (int i = 0; i < pageSize + 1; i++) {
            company = generateCompanyWithRandomEmailAndIsSoftDeleted(random, false);
            softDeleted = generateCompanyWithRandomEmailAndIsSoftDeleted(random, true);
            companies.add(company);
            companies.add(softDeleted);
            underTest.save(company);
            underTest.save(softDeleted);
        }

        // when
        List<Company> firstPage = underTest.getAll(0, Collections.emptyMap());
        List<Company> secondPage = underTest.getAll(1, Collections.emptyMap());

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize);
        Assertions.assertThat(firstPage).noneMatch(company1 -> company1.getIsDeleted().equals(true));
        Assertions.assertThat(secondPage.size()).isEqualTo(companies.size() / 2 - pageSize);
        Assertions.assertThat(secondPage).noneMatch(company1 -> company1.getIsDeleted().equals(true));

        // clean up
        deleteAllCompanies(companies);
    }

    @Test
    public void canGetAllCompaniesFilteredByEmail() {
        // given
        List<Company> companies = new ArrayList<>();
        Company company;
        Company company2;
        Random random = new Random();
        for (int i = 0; i < pageSize / 2; i++) {
            company = generateCompanyWithRandomizedEmail(random, "email%s@email.com");
            company2 = generateCompanyWithRandomizedEmail(random, "company%s@company.com");
            companies.add(company);
            companies.add(company2);
            underTest.save(company);
            underTest.save(company2);
        }
        Map<String, String> emailFilter = new HashMap<>();
        String filterValue = "company";
        emailFilter.put("email", filterValue);

        // when
        List<Company> firstPage = underTest.getAll(0, emailFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize / 2);
        Assertions.assertThat(firstPage).allMatch(company1 -> company1.getEmail().contains(filterValue));

        // clean up
        deleteAllCompanies(companies);
    }

    @Test
    public void canGetAllCompaniesFilteredByName() {
        // given
        List<Company> companies = new ArrayList<>();
        Company company;
        Company company2;
        Random random = new Random();
        for (int i = 0; i < pageSize / 2; i++) {
            company = generateCompanyWithRandomEmailAndName(random, "name");
            company2 = generateCompanyWithRandomEmailAndName(random, "absolutely different name");
            companies.add(company);
            companies.add(company2);
            underTest.save(company);
            underTest.save(company2);
        }
        Map<String, String> emailFilter = new HashMap<>();
        String filterValue = "absolutely different name";
        emailFilter.put("name", filterValue);

        // when
        List<Company> firstPage = underTest.getAll(0, emailFilter);

        // then
        Assertions.assertThat(firstPage.size()).isEqualTo(pageSize / 2);
        Assertions.assertThat(firstPage).allMatch(company1 -> company1.getName().contains(filterValue));

        // clean up
        deleteAllCompanies(companies);
    }

    @Test
    public void canUpdateCompany() {
        // given
        Company updatedFields = new Company(null, "upd name",
                "updEmail@email.com", null, "upd description", null);
        Company company = generateDefaultCompanyWithIsSoftDeleted(false);
        underTest.save(company);

        Company expected = new Company(company.getId(), updatedFields.getName(),
                updatedFields.getEmail(), company.getCreated(), updatedFields.getDescription(),
                company.getIsDeleted());

        // when
        underTest.update(company.getId(), updatedFields);
        Company updated = underTest.get(company.getId());

        // then
        Assertions.assertThat(updated).isEqualTo(expected);

        // clean up
        deleteCompany(company);
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        Company updatedFields = new Company();
        Company company = generateDefaultCompanyWithIsSoftDeleted(false);
        underTest.save(company);

        // when
        underTest.update(company.getId(), updatedFields);
        Company updated = underTest.get(company.getId());

        // then
        Assertions.assertThat(updated).isEqualTo(company);

        // clean up
        deleteCompany(company);
    }


    private void deleteCompany(Company company) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    private void deleteAllCompanies(Collection<Company> companies) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        companies.forEach(session::delete);
        transaction.commit();
        session.close();
    }

    private Company generateDefaultCompanyWithIsSoftDeleted(boolean isDeleted) {
        return new Company(null, "name", "email@email.com",
                now().truncatedTo(SECONDS), "description", isDeleted);
    }

    private Company generateCompanyWithRandomEmailAndIsSoftDeleted(Random random, boolean isDeleted) {
        return new Company(null, "name", format("email%s@email.com", random.nextInt()),
                now().truncatedTo(SECONDS), "description", isDeleted);
    }

    private Company generateCompanyWithRandomizedEmail(Random random, String emailFormat) {
        return new Company(null, "name", format(emailFormat, random.nextInt()),
                now().truncatedTo(SECONDS), "description", false);
    }

    private Company generateCompanyWithRandomEmailAndName(Random random, String name) {
        return new Company(null, name, format("email%s@email.com", random.nextInt()),
                now().truncatedTo(SECONDS), "description", false);
    }
}

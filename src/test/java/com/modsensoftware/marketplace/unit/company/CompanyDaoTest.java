package com.modsensoftware.marketplace.unit.company;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.dao.CompanyDao;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CompanyDaoTest {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CompanyDao underTest;

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    @Value("${default.page.size}")
    private int pageSize;

    @Test
    public void canSaveCompany() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
        // when
        underTest.save(company);
        // then
        Company saved = sessionFactory.openSession().get(Company.class, company.getId());
        Assertions.assertThat(saved).isEqualTo(company);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canSoftDeleteById() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
        // when
        underTest.save(company);
        underTest.deleteById(company.getId());

        // then
        Company shouldBeSoftDeleted = sessionFactory.openSession().get(Company.class, company.getId());
        Assertions.assertThat(shouldBeSoftDeleted.getIsDeleted()).isTrue();

        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetById() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
        underTest.save(company);
        // when
        Company saved = underTest.get(company.getId());
        // then
        Assertions.assertThat(saved).isEqualTo(company);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void getByIdShouldThrowEntityNotFoundExceptionIfSoftDeleted() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", true);
        underTest.save(company);
        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.get(company.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format("Company entity with id=%s is not present.", company.getId()));
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void shouldExistByEmail() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
        underTest.save(company);
        // when
        boolean existsByEmail = underTest.existsByEmail(company.getEmail());
        // then
        Assertions.assertThat(existsByEmail).isTrue();
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void existsByEmailShouldBeFalseIfSoftDeleted() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", true);
        underTest.save(company);
        // when
        boolean existsByEmail = underTest.existsByEmail(company.getEmail());
        // then
        Assertions.assertThat(existsByEmail).isFalse();
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void existsByEmailShouldBeFalseIfEntityNotFound() {
        // given
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
        underTest.save(company);
        // when
        boolean existsByEmail = underTest.existsByEmail(company.getEmail() + "dsahjshfjdskfd");
        // then
        Assertions.assertThat(existsByEmail).isFalse();
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllNonSoftDeletedCompanies() {
        // given
        List<Company> companies = new ArrayList<>();
        Company company;
        Company softDeleted;
        Random random = new Random();
        for (int i = 0; i < pageSize + 1; i++) {
            company = new Company(null, "name", format("email%s@email.com", random.nextInt()),
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
            softDeleted = new Company(null, "name", format("email%s@email.com", random.nextInt()),
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", true);
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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        companies.forEach(session::delete);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllCompaniesFilteredByEmail() {
        // given
        List<Company> companies = new ArrayList<>();
        Company company;
        Company company2;
        Random random = new Random();
        for (int i = 0; i < pageSize / 2; i++) {
            company = new Company(null, "name", format("email%s@email.com", random.nextInt()),
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
            company2 = new Company(null, "name", format("company%s@company.com", random.nextInt()),
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        companies.forEach(session::delete);
        transaction.commit();
        session.close();
    }

    @Test
    public void canGetAllCompaniesFilteredByName() {
        // given
        List<Company> companies = new ArrayList<>();
        Company company;
        Company company2;
        Random random = new Random();
        for (int i = 0; i < pageSize / 2; i++) {
            company = new Company(null, "name", format("email%s@email.com", random.nextInt()),
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
            company2 = new Company(null, "absolutely different name", format("email%s@email.com", random.nextInt()),
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        companies.forEach(session::delete);
        transaction.commit();
        session.close();
    }

    @Test
    public void canUpdateCompany() {
        // given
        Company updatedFields = new Company();
        updatedFields.setDescription("upd description");
        updatedFields.setName("upd name");
        updatedFields.setEmail("updEmail@email.com");
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
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
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

    @Test
    public void noUpdateShouldBeExecutedIfNoUpdatedFieldsAreProvided() {
        // given
        Company updatedFields = new Company();
        Company company = new Company(null, "name", "email@email.com",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), "description", false);
        underTest.save(company);

        // when
        underTest.update(company.getId(), updatedFields);
        Company updated = underTest.get(company.getId());
        // then
        Assertions.assertThat(updated).isEqualTo(company);
        // clean up
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.delete(company);
        transaction.commit();
        session.close();
    }

}

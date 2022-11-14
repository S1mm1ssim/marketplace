package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Component
public class CompanyDao implements Dao<Company, Long> {

    private static final String COMPANY_TABLE_NAME = "company";
    private static final String SELECT = format("SELECT id, name, email, created, description FROM %s",
            COMPANY_TABLE_NAME);
    private static final String SELECT_BY_ID = SELECT + " WHERE id=?";
    private static final String INSERT = format("INSERT INTO %s(name, email, created, description) VALUES(?, ?, ?, ?)",
            COMPANY_TABLE_NAME);
    private static final String UPDATE = format("UPDATE %s SET name=?, email=?, created=?, description=? WHERE id=?",
            COMPANY_TABLE_NAME);
    private static final String DELETE = format("DELETE FROM %s WHERE id=?", COMPANY_TABLE_NAME);

    private static final String ID_COLUMN_NAME = "id";
    private static final String NAME_COLUMN_NAME = "name";
    private static final String EMAIL_COLUMN_NAME = "email";
    private static final String CREATED_COLUMN_NAME = "created";
    private static final String DESCRIPTION_COLUMN_NAME = "description";

    @Override
    public Optional<Company> get(Long id) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Company company = new Company(rs.getLong(ID_COLUMN_NAME), rs.getString(NAME_COLUMN_NAME),
                        rs.getString(EMAIL_COLUMN_NAME), rs.getTimestamp(CREATED_COLUMN_NAME).toLocalDateTime(),
                        rs.getString(DESCRIPTION_COLUMN_NAME)
                );
                return Optional.of(company);
            }
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT by id={}.", id);
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Company> getAll() {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT);
            ResultSet rs = ps.executeQuery();
            List<Company> companies = new ArrayList<>();
            while (rs.next()) {
                Company company = new Company(rs.getLong(ID_COLUMN_NAME), rs.getString(NAME_COLUMN_NAME),
                        rs.getString(EMAIL_COLUMN_NAME), rs.getTimestamp(CREATED_COLUMN_NAME).toLocalDateTime(),
                        rs.getString(DESCRIPTION_COLUMN_NAME)
                );
                companies.add(company);
            }
            return companies;
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT all");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void save(Company company) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(INSERT);
            ps.setString(1, company.getName());
            ps.setString(2, company.getEmail());
            ps.setTimestamp(3, Timestamp.valueOf(company.getCreated()));
            ps.setString(4, company.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during INSERT");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    @Override
    public void update(Long id, Company updatedFields) {
        Optional<Company> optionalCompany = get(id);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.orElseThrow();
            setIfNotNull(updatedFields.getName(), company::setName);
            setIfNotNull(updatedFields.getEmail(), company::setEmail);
            setIfNotNull(updatedFields.getCreated(), company::setCreated);
            setIfNotNull(updatedFields.getDescription(), company::setDescription);

            try (Connection connection = DataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement(UPDATE);
                ps.setString(1, company.getName());
                ps.setString(2, company.getEmail());
                ps.setTimestamp(3, Timestamp.valueOf(company.getCreated()));
                ps.setString(4, company.getDescription());
                ps.setLong(5, company.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("SQL Exception caught during UPDATE");
                if (log.isDebugEnabled()) {
                    log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
                }
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(DELETE);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during DELETE by id={}", id);
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }
}

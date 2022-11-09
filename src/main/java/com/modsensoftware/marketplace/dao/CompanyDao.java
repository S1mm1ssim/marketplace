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

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Component
public class CompanyDao implements Dao<Company, Long> {

    private static final String SELECT = "SELECT id, name, email, created, description FROM company";
    private static final String SELECT_BY_ID = SELECT + " WHERE id=?";
    private static final String INSERT = "INSERT INTO company(name, email, created, description) VALUES(?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE company SET name=?, email=?, created=?, description=? WHERE id=?";
    private static final String DELETE = "DELETE FROM company WHERE id=?";

    @Override
    public Optional<Company> get(Long id) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Company company = new Company(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getTimestamp("created").toLocalDateTime(), rs.getString("description")
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
                Company company = new Company(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getTimestamp("created").toLocalDateTime(), rs.getString("description")
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

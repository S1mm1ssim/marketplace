package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.enums.Role;
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
import java.util.UUID;

import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
@Slf4j
@Component
public class UserDao implements Dao<User, UUID> {

    private static final String SELECT = "SELECT u.id AS user_id, u.username AS user_username, u.email AS user_email, "
            + "u.name AS user_name, u.role AS user_role, u.created AS user_created, "
            + "u.updated AS user_updated, u.company_id AS fk_user_company, "
            + "c.id AS company_id, c.name AS company_name, c.email AS company_email, "
            + "c.created AS company_created, c.description AS company_description "
            + "FROM \"user\" AS u "
            + "INNER JOIN company AS c on u.company_id = c.id ";
    private static final String SELECT_BY_ID = SELECT + " WHERE u.id=?";
    private static final String INSERT = "INSERT INTO \"user\"(username, email, name, role, "
            + "created, updated, company_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE \"user\" SET username=?, email=?, name=?,"
            + "role=?, created=?, updated=?, company_id=? WHERE id=?";
    private static final String DELETE = "DELETE FROM \"user\" WHERE id=?";

    @Override
    public Optional<User> get(UUID id) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID);
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(createUserFromResultSetRow(rs));
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
    public List<User> getAll() {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT);
            ResultSet rs = ps.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(createUserFromResultSetRow(rs));
            }
            return users;
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT all");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void save(User user) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(INSERT);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getName());
            ps.setString(4, user.getRole().toString());
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreated()));
            ps.setTimestamp(6, Timestamp.valueOf(user.getUpdated()));
            ps.setLong(7, user.getCompany().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during INSERT");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    @Override
    public void update(UUID id, User updatedFields) {
        Optional<User> optionalUser = get(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.orElseThrow();
            setIfNotNull(updatedFields.getUsername(), user::setUsername);
            setIfNotNull(updatedFields.getEmail(), user::setEmail);
            setIfNotNull(updatedFields.getName(), user::setName);
            setIfNotNull(updatedFields.getRole(), user::setRole);
            setIfNotNull(updatedFields.getCreated(), user::setCreated);
            setIfNotNull(updatedFields.getUpdated(), user::setUpdated);
            setIfNotNull(updatedFields.getCompany().getId(),
                    (value) -> user.getCompany().setId(value));

            try (Connection connection = DataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement(UPDATE);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getName());
                ps.setString(4, user.getRole().toString());
                ps.setTimestamp(5, Timestamp.valueOf(user.getCreated()));
                ps.setTimestamp(6, Timestamp.valueOf(user.getUpdated()));
                ps.setLong(7, user.getCompany().getId());
                ps.setObject(8, user.getId());
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
    public void deleteById(UUID id) {
        try (Connection connection = DataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(DELETE);
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during DELETE by id={}", id);
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    private User createUserFromResultSetRow(ResultSet rs) throws SQLException {
        Company company = new Company(rs.getLong("company_id"), rs.getString("company_name"),
                rs.getString("company_email"), rs.getTimestamp("company_created").toLocalDateTime(),
                rs.getString("company_description"));
        return new User(UUID.fromString(rs.getString("user_id")),
                rs.getString("user_username"), rs.getString("user_email"), rs.getString("user_name"),
                Role.valueOf(rs.getString("user_role")), rs.getTimestamp("user_created").toLocalDateTime(),
                rs.getTimestamp("user_updated").toLocalDateTime(), company
        );
    }
}

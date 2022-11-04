package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
@Slf4j
@Component
public class UserDao implements Dao<User, UUID> {

    @Override
    public Optional<User> get(UUID id) {
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT u.*, c.* FROM \"user\" AS u " +
                    "INNER JOIN company AS c on u.company_id = c.id " +
                    "WHERE u.id=?";
            PreparedStatement ps = connection.prepareStatement(query);
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
            String query = "SELECT u.*, c.* FROM \"user\" AS u " +
                    "INNER JOIN company c on u.company_id = c.id";
            PreparedStatement ps = connection.prepareStatement(query);
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
            String insertQuery = "INSERT INTO \"user\"(username, email, name, role, created, updated, company_id) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(insertQuery);
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
                String updateQuery = "UPDATE \"user\" SET username=?, email=?, name=?," +
                        "role=?, created=?, updated=?, company_id=? WHERE id=?";
                PreparedStatement ps = connection.prepareStatement(updateQuery);
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
            String deleteQuery = "DELETE FROM \"user\" WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);
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
        return new User(UUID.fromString(rs.getString(1)),
                rs.getString(2), rs.getString(3), rs.getString(4),
                Role.valueOf(rs.getString(5)), rs.getTimestamp(6).toLocalDateTime(),
                rs.getTimestamp(7).toLocalDateTime(),
                new Company(rs.getLong(9), rs.getString(10), rs.getString(11),
                        rs.getTimestamp(12).toLocalDateTime(), rs.getString(13))
        );
    }
}

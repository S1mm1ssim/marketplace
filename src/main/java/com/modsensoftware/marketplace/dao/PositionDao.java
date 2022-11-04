package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.*;
import com.modsensoftware.marketplace.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

import static com.modsensoftware.marketplace.utils.Utils.setIfNotNull;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Component
public class PositionDao implements Dao<Position, Long> {
    @Override
    public Optional<Position> get(Long id) {
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT p.*, i.*, c.*, u.* FROM position AS p " +
                    "INNER JOIN company AS c ON p.company_id = c.id " +
                    "INNER JOIN item AS i ON p.item_id = i.id " +
                    "INNER JOIN \"user\" AS u ON p.created_by = u.id " +
                    "WHERE p.id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(createPositionFromResultSetRow(rs));
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
    public List<Position> getAll() {
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT p.*, i.*, c.*, u.* FROM position AS p " +
                    "INNER JOIN company AS c ON p.company_id = c.id " +
                    "INNER JOIN item AS i ON p.item_id = i.id " +
                    "INNER JOIN \"user\" AS u ON p.created_by = u.id";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            List<Position> positions = new ArrayList<>();
            while (rs.next()) {
                positions.add(createPositionFromResultSetRow(rs));
            }
            return positions;
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT all");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void save(Position position) {
        try (Connection connection = DataSource.getConnection()) {
            String insertQuery = "INSERT INTO position(item_id, company_id, created_by, created, amount) " +
                    "VALUES(?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(insertQuery);
            ps.setObject(1, position.getItem().getId());
            ps.setLong(2, position.getCompany().getId());
            ps.setObject(3, position.getCreatedBy().getId());
            ps.setTimestamp(4, Timestamp.valueOf(position.getCreated()));
            ps.setDouble(5, position.getAmount());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during INSERT");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    @Override
    public void update(Long id, Position updatedFields) {
        Optional<Position> optionalPosition = get(id);
        if (optionalPosition.isPresent()) {
            Position position = optionalPosition.orElseThrow();
            setIfNotNull(updatedFields.getItem().getId(),
                    (value) -> position.getItem().setId(value));
            setIfNotNull(updatedFields.getCompany().getId(),
                    (value) -> position.getCompany().setId(value));
            setIfNotNull(updatedFields.getCreatedBy().getId(),
                    (value) -> position.getCreatedBy().setId(value));
            setIfNotNull(updatedFields.getCreated(), position::setCreated);
            setIfNotNull(updatedFields.getAmount(), position::setAmount);

            try (Connection connection = DataSource.getConnection()) {
                String updateQuery = "UPDATE position SET item_id = ?, company_id = ?, " +
                        "created_by = ?, created = ?, amount = ? WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(updateQuery);
                ps.setObject(1, position.getItem().getId());
                ps.setLong(2, position.getCompany().getId());
                ps.setObject(3, position.getCreatedBy().getId());
                ps.setTimestamp(4, Timestamp.valueOf(position.getCreated()));
                ps.setDouble(5, position.getAmount());
                ps.setLong(6, position.getId());
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
            String deleteQuery = "DELETE FROM position WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT by id={}.", id);
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    private Position createPositionFromResultSetRow(ResultSet rs) throws SQLException {
        Item item = new Item(UUID.fromString(rs.getString(7)), rs.getString(8), rs.getString(9), rs.getTimestamp(10).toLocalDateTime(), new Category());
        Company company = new Company(rs.getLong(12), rs.getString(13), rs.getString(14), rs.getTimestamp(15).toLocalDateTime(), rs.getString(16));
        User user = new User(UUID.fromString(rs.getString(17)), rs.getString(18), rs.getString(19), rs.getString(20), Role.valueOf(rs.getString(21)), rs.getTimestamp(22).toLocalDateTime(), rs.getTimestamp(23).toLocalDateTime(), company);
        return new Position(rs.getLong(1), item, company, user, rs.getTimestamp(5).toLocalDateTime(), rs.getDouble(6));
    }
}

package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.enums.Role;
import lombok.RequiredArgsConstructor;
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
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PositionDao implements Dao<Position, Long> {

    private final DataSource dataSource;

    private static final String SELECT = "SELECT p.id AS position_id, p.item_id AS fk_position_item, "
            + "p.company_id AS fk_position_company, p.created_by AS fk_position_user, "
            + "p.created AS position_created, p.amount AS position_amount, "
            + "i.id AS item_id, i.name AS item_name, "
            + "i.description AS item_description, i.created AS item_created, "
            + "i.category_id AS fk_item_category, "
            + "categ.id AS category_id, categ.name AS category_name, "
            + "categ.parent_category AS fk_category_parent, categ.description AS category_description, "
            + "c.id AS company_id, c.name AS company_name, c.email AS company_email, "
            + "c.created AS company_created, c.description AS company_description, "
            + "u.id AS user_id, u.username AS user_username, u.email AS user_email, "
            + "u.name AS user_name, u.role AS user_role, u.created AS user_created,"
            + "u.updated AS user_updated, u.company_id AS fk_user_company "
            + "FROM position AS p "
            + "INNER JOIN company AS c ON p.company_id = c.id "
            + "INNER JOIN item AS i ON p.item_id = i.id "
            + "INNER JOIN category categ on i.category_id = categ.id "
            + "INNER JOIN \"user\" AS u ON p.created_by = u.id";
    private static final String SELECT_BY_ID = SELECT + " WHERE p.id = ?";
    private static final String INSERT = "INSERT INTO position(item_id, company_id, created_by, created, amount) "
            + "VALUES(?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE position SET item_id = ?, company_id = ?, "
            + "created_by = ?, created = ?, amount = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM position WHERE id = ?";

    @Override
    public Optional<Position> get(Long id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID);
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
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT);
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
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(INSERT);
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

            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement(UPDATE);
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
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(DELETE);
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
        Category category = new Category(rs.getLong("category_id"),
                rs.getString("category_name"), rs.getString("category_description"), new Category());
        Item item = new Item(UUID.fromString(rs.getString("item_id")), rs.getString("item_name"),
                rs.getString("item_description"), rs.getTimestamp("item_created").toLocalDateTime(), category);
        Company company = new Company(rs.getLong("company_id"), rs.getString("company_name"),
                rs.getString("company_email"), rs.getTimestamp("company_created").toLocalDateTime(),
                rs.getString("company_description"));
        User user = new User(UUID.fromString(rs.getString("user_id")), rs.getString("user_username"),
                rs.getString("user_email"), rs.getString("user_name"), Role.valueOf(rs.getString("user_role")),
                rs.getTimestamp("user_created").toLocalDateTime(),
                rs.getTimestamp("user_updated").toLocalDateTime(), company);

        return new Position(rs.getLong("position_id"), item, company, user,
                rs.getTimestamp("position_created").toLocalDateTime(), rs.getDouble("position_amount"));
    }
}

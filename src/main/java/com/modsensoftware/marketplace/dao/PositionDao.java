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
import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/2/2022
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PositionDao implements Dao<Position, Long> {

    private final DataSource dataSource;

    private static final String USER_TABLE_NAME = "\"user\"";
    private static final String COMPANY_TABLE_NAME = "company";
    private static final String ITEM_TABLE_NAME = "item";
    private static final String CATEGORY_TABLE_NAME = "category";
    private static final String POSITION_TABLE_NAME = "position";

    private static final String SELECT = format("SELECT p.id AS position_id, p.item_id AS fk_position_item, "
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
            + "FROM %s AS p "
            + "INNER JOIN %s AS c ON p.company_id = c.id "
            + "INNER JOIN %s AS i ON p.item_id = i.id "
            + "INNER JOIN %s categ on i.category_id = categ.id "
            + "INNER JOIN %s AS u ON p.created_by = u.id", POSITION_TABLE_NAME,
            COMPANY_TABLE_NAME, ITEM_TABLE_NAME, CATEGORY_TABLE_NAME, USER_TABLE_NAME);
    private static final String SELECT_BY_ID = SELECT + " WHERE p.id = ?";
    private static final String INSERT = format("INSERT INTO %s(item_id, company_id, created_by, created, amount) "
            + "VALUES(?, ?, ?, ?, ?)", POSITION_TABLE_NAME);
    private static final String UPDATE = format("UPDATE %s SET item_id = ?, company_id = ?, "
            + "created_by = ?, created = ?, amount = ? WHERE id = ?", POSITION_TABLE_NAME);
    private static final String DELETE = format("DELETE FROM %S WHERE id = ?", POSITION_TABLE_NAME);

    private static final String COMPANY_ID = "company_id";
    private static final String COMPANY_NAME = "company_name";
    private static final String COMPANY_EMAIL = "company_email";
    private static final String COMPANY_CREATED = "company_created";
    private static final String COMPANY_DESCRIPTION = "company_description";
    private static final String USER_ID = "user_id";
    private static final String USER_USERNAME = "user_username";
    private static final String USER_EMAIL = "user_email";
    private static final String USER_NAME = "user_name";
    private static final String USER_ROLE = "user_role";
    private static final String USER_CREATED = "user_created";
    private static final String USER_UPDATED = "user_updated";
    private static final String CATEGORY_ID = "category_id";
    private static final String CATEGORY_NAME = "category_name";
    private static final String CATEGORY_DESCRIPTION = "category_description";
    private static final String ITEM_ID = "item_id";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_DESCRIPTION = "item_description";
    private static final String ITEM_CREATED = "item_created";
    private static final String POSITION_ID = "position_id";
    private static final String POSITION_CREATED = "position_created";
    private static final String POSITION_AMOUNT = "position_amount";

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
        Category category = new Category(rs.getLong(CATEGORY_ID),
                rs.getString(CATEGORY_NAME), rs.getString(CATEGORY_DESCRIPTION), new Category());
        Item item = new Item(UUID.fromString(rs.getString(ITEM_ID)), rs.getString(ITEM_NAME),
                rs.getString(ITEM_DESCRIPTION), rs.getTimestamp(ITEM_CREATED).toLocalDateTime(), category);
        Company company = new Company(rs.getLong(COMPANY_ID), rs.getString(COMPANY_NAME),
                rs.getString(COMPANY_EMAIL), rs.getTimestamp(COMPANY_CREATED).toLocalDateTime(),
                rs.getString(COMPANY_DESCRIPTION));
        User user = new User(UUID.fromString(rs.getString(USER_ID)), rs.getString(USER_USERNAME),
                rs.getString(USER_EMAIL), rs.getString(USER_NAME), Role.valueOf(rs.getString(USER_ROLE)),
                rs.getTimestamp(USER_CREATED).toLocalDateTime(),
                rs.getTimestamp(USER_UPDATED).toLocalDateTime(), company);

        return new Position(rs.getLong(POSITION_ID), item, company, user,
                rs.getTimestamp(POSITION_CREATED).toLocalDateTime(), rs.getDouble(POSITION_AMOUNT));
    }
}

package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
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
public class ItemDao implements Dao<Item, UUID> {

    private final DataSource dataSource;

    private static final String ITEM_TABLE_NAME = "item";
    private static final String CATEGORY_TABLE_NAME = "category";

    private static final String SELECT = format("SELECT i.id AS item_id, i.name AS item_name, "
                    + "i.description AS item_description, i.created AS item_created, "
                    + "i.category_id AS fk_item_category, "
                    + "c.id AS category_id, c.name AS category_name, "
                    + "c.parent_category AS fk_category_parent, c.description AS category_description "
                    + "FROM %s i "
                    + "INNER JOIN %s c on i.category_id = c.id "
                    + "LEFT JOIN %s pc on pc.id = c.parent_category", ITEM_TABLE_NAME,
            CATEGORY_TABLE_NAME, CATEGORY_TABLE_NAME);

    private static final String SELECT_BY_ID = SELECT + " WHERE i.id = ?";

    private static final String INSERT = format("INSERT INTO %s(name, description, created, category_id) "
            + "VALUES(?, ?, ?, ?)", ITEM_TABLE_NAME);

    private static final String UPDATE = format("UPDATE %s SET name = ?, "
            + "description = ?, created = ?, category_id = ? WHERE id = ?", ITEM_TABLE_NAME);

    private static final String DELETE = format("DELETE FROM %s WHERE id=?", ITEM_TABLE_NAME);

    private static final String CATEGORY_ID = "category_id";
    private static final String CATEGORY_NAME = "category_name";
    private static final String CATEGORY_DESCRIPTION = "category_description";
    private static final String ITEM_ID = "item_id";
    private static final String ITEM_NAME = "item_name";
    private static final String ITEM_DESCRIPTION = "item_description";
    private static final String ITEM_CREATED = "item_created";

    @Override
    public Optional<Item> get(UUID id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID);
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(createItemFromResultSet(rs));
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
    public List<Item> getAll() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT);
            ResultSet rs = ps.executeQuery();
            List<Item> items = new ArrayList<>();
            while (rs.next()) {
                items.add(createItemFromResultSet(rs));
            }
            return items;
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT all");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void save(Item item) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(INSERT);
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(item.getCreated()));
            ps.setLong(4, item.getCategory().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during INSERT");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    @Override
    public void update(UUID id, Item updatedFields) {
        Optional<Item> optionalItem = get(id);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.orElseThrow();
            setIfNotNull(updatedFields.getName(), item::setName);
            setIfNotNull(updatedFields.getDescription(), item::setDescription);
            setIfNotNull(updatedFields.getCreated(), item::setCreated);
            setIfNotNull(updatedFields.getCategory().getId(),
                    (value) -> item.getCategory().setId(value));

            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement(UPDATE);
                ps.setString(1, item.getName());
                ps.setString(2, item.getDescription());
                ps.setTimestamp(3, Timestamp.valueOf(item.getCreated()));
                ps.setLong(4, item.getCategory().getId());
                ps.setObject(5, item.getId());
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
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(DELETE);
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during DELETE by id={}", id.toString());
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    private Item createItemFromResultSet(ResultSet rs) throws SQLException {
        Category category = new Category(rs.getLong(CATEGORY_ID), rs.getString(CATEGORY_NAME),
                rs.getString(CATEGORY_DESCRIPTION), null);
        return new Item(
                UUID.fromString(rs.getString(ITEM_ID)), rs.getString(ITEM_NAME), rs.getString(ITEM_DESCRIPTION),
                rs.getTimestamp(ITEM_CREATED).toLocalDateTime(), category
        );
    }
}

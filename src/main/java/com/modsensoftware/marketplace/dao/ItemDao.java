package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
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
public class ItemDao implements Dao<Item, UUID> {

    @Override
    public Optional<Item> get(UUID id) {
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT i.*, c.* FROM item i " +
                    "INNER JOIN category c on i.category_id = c.id " +
                    "INNER JOIN category pc on pc.id = c.parent_category " +
                    "WHERE i.id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
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
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT i.*, c.* FROM item AS i " +
                    "INNER JOIN category c on i.category_id = c.id " +
                    "INNER JOIN category pc on pc.id = c.parent_category";
            PreparedStatement ps = connection.prepareStatement(query);
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
        try (Connection connection = DataSource.getConnection()) {
            String insertQuery = "INSERT INTO item(name, description, created, category_id) VALUES(?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(insertQuery);
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

            try (Connection connection = DataSource.getConnection()) {
                String updateQuery = "UPDATE item SET name=?," +
                        "description=?, created=?, category_id=? WHERE id=?";
                PreparedStatement ps = connection.prepareStatement(updateQuery);
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
        try (Connection connection = DataSource.getConnection()) {
            String deleteQuery = "DELETE FROM item WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);
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
        return new Item(
                UUID.fromString(rs.getString(1)), rs.getString(2), rs.getString(3),
                rs.getTimestamp(4).toLocalDateTime(),
                new Category(rs.getLong(6), rs.getString(7), rs.getString(9),
                        new Category())
        );
    }
}

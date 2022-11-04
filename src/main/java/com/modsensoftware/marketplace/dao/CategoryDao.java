package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
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
public class CategoryDao implements Dao<Category, Long> {

    private static final int PARENT_CATEGORY_COLUMN_ID = 3;

    @Override
    public Optional<Category> get(Long id) {
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT c.*, p.* FROM category c " +
                    "LEFT JOIN category p ON p.id = c.parent_category " +
                    "WHERE c.id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // In createCategoryFromResultSet method rs.wasNull() is called.
                // So we check if parent_category is null
                rs.getLong(PARENT_CATEGORY_COLUMN_ID);
                return Optional.of(createCategoryFromResultSet(rs));
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
    public List<Category> getAll() {
        try (Connection connection = DataSource.getConnection()) {
            String query = "SELECT c.*, p.* FROM category c " +
                    "LEFT JOIN category p ON p.id = c.parent_category";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                // In createCategoryFromResultSet method rs.wasNull() is called.
                // So we check if parent_category is null
                rs.getLong(PARENT_CATEGORY_COLUMN_ID);
                categories.add(createCategoryFromResultSet(rs));
            }
            return categories;
        } catch (SQLException e) {
            log.error("SQL Exception caught during SELECT all");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void save(Category category) {
        try (Connection connection = DataSource.getConnection()) {
            String insertQuery = "INSERT INTO category(name, parent_category, description) " +
                    "VALUES(?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(insertQuery);
            ps.setString(1, category.getName());

            if (category.getParent().getId() != null) {
                ps.setLong(2, category.getParent().getId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setString(3, category.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during INSERT");
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    @Override
    public void update(Long id, Category updatedFields) {
        Optional<Category> optionalCategory = get(id);
        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.orElseThrow();
            Optional.ofNullable(updatedFields.getName()).ifPresent(category::setName);
            setIfNotNull(updatedFields.getName(), category::setName);
            setIfNotNull(updatedFields.getParent().getId(),
                    value -> category.getParent().setId(value));
            setIfNotNull(updatedFields.getDescription(), category::setDescription);

            try (Connection connection = DataSource.getConnection()) {
                String updateQuery = "UPDATE category SET name = ?, " +
                        "description = ?, parent_category = ? WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(updateQuery);
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                if (category.getParent() != null && category.getParent().getId() != null) {
                    ps.setLong(3, category.getParent().getId());
                } else {
                    ps.setNull(3, Types.BIGINT);
                }
                ps.setLong(4, id);
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
            String deleteQuery = "DELETE FROM category WHERE id=?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Exception caught during DELETE by id={}", id);
            if (log.isDebugEnabled()) {
                log.debug("SQL state - {}. Stacktrace:\n{}", e.getSQLState(), e.getMessage());
            }
        }
    }

    private Category createCategoryFromResultSet(ResultSet rs) throws SQLException {
        Category category;
        if (rs.wasNull()) {
            category = new Category(rs.getLong(1),
                    rs.getString(2), rs.getString(4), null);
        } else {
            category = new Category(rs.getLong(1),
                    rs.getString(2), rs.getString(4),
                    new Category(rs.getLong(5), rs.getString(6), rs.getString(8), null));
        }
        return category;
    }
}

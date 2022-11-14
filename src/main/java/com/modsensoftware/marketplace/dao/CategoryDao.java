package com.modsensoftware.marketplace.dao;

import com.modsensoftware.marketplace.config.DataSource;
import com.modsensoftware.marketplace.domain.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
@RequiredArgsConstructor
public class CategoryDao implements Dao<Category, Long> {

    private final DataSource dataSource;

    private static final String CATEGORY_TABLE_NAME = "category";

    private static final String SELECT = format("SELECT c.id AS category_id, c.name AS category_name, "
            + "c.parent_category AS fk_parent_category, c.description AS category_description, "
            + "p.id AS parent_id, p.name AS parent_name, p.description AS parent_description "
            + "FROM %s c "
            + "LEFT JOIN %s p ON p.id = c.parent_category", CATEGORY_TABLE_NAME, CATEGORY_TABLE_NAME);
    private static final String SELECT_BY_ID = SELECT + " WHERE c.id = ?";
    private static final String INSERT_INTO = format("INSERT INTO %s(name, parent_category, description) "
            + "VALUES(?, ?, ?)", CATEGORY_TABLE_NAME);
    private static final String UPDATE = format("UPDATE %s SET name = ?, "
            + "description = ?, parent_category = ? WHERE id = ?", CATEGORY_TABLE_NAME);
    private static final String DELETE = format("DELETE FROM %s WHERE id=?", CATEGORY_TABLE_NAME);

    private static final String CATEGORY_ID = "category_id";
    private static final String CATEGORY_NAME = "category_name";
    private static final String CATEGORY_DESCRIPTION = "category_description";
    private static final String FK_PARENT_CATEGORY = "fk_parent_category";
    private static final String PARENT_ID = "parent_id";
    private static final String PARENT_NAME = "parent_name";
    private static final String PARENT_DESCRIPTION = "parent_description";

    @Override
    public Optional<Category> get(Long id) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // In createCategoryFromResultSet method rs.wasNull() is called.
                // So we check if parent_category is null
                rs.getLong(FK_PARENT_CATEGORY);
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
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(SELECT);
            ResultSet rs = ps.executeQuery();
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                // In createCategoryFromResultSet method rs.wasNull() is called.
                // So we check if parent_category is null
                rs.getLong(FK_PARENT_CATEGORY);
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
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(INSERT_INTO);
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
            if (updatedFields.getParent() != null) {
                setIfNotNull(updatedFields.getParent().getId(),
                        value -> category.getParent().setId(value));
            } else {
                category.setParent(null);
            }
            setIfNotNull(updatedFields.getDescription(), category::setDescription);

            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement(UPDATE);
                ps.setString(1, category.getName());
                ps.setString(2, category.getDescription());
                if (category.getParent() != null) {
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
        try (Connection connection = dataSource.getConnection()) {
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

    private Category createCategoryFromResultSet(ResultSet rs) throws SQLException {
        Category category;
        if (rs.wasNull()) {
            category = new Category(rs.getLong(CATEGORY_ID),
                    rs.getString(CATEGORY_NAME), rs.getString(CATEGORY_DESCRIPTION), null);
        } else {
            category = new Category(rs.getLong(CATEGORY_ID),
                    rs.getString(CATEGORY_NAME), rs.getString(CATEGORY_DESCRIPTION),
                    new Category(rs.getLong(PARENT_ID), rs.getString(PARENT_NAME),
                            rs.getString(PARENT_DESCRIPTION), null));
        }
        return category;
    }
}

package com.nadia.pos.dao;

import com.nadia.pos.model.BaseEntity;
import com.nadia.pos.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseDAOImpl<T extends BaseEntity> implements BaseDAO<T> {
    protected final Connection connection;
    protected final String tableName;

    protected BaseDAOImpl(String tableName) throws SQLException {
        this.connection = DatabaseUtil.getConnection();
        this.tableName = tableName;
    }

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract void setStatementParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract String getInsertQuery();
    protected abstract String getUpdateQuery();

    @Override
    public T save(T entity) {
        try {
            if (entity.getId() == null) {
                try (PreparedStatement stmt = connection.prepareStatement(getInsertQuery(), Statement.RETURN_GENERATED_KEYS)) {
                    setStatementParameters(stmt, entity);
                    stmt.executeUpdate();

                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            entity.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } else {
                update(entity);
            }
            return entity;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error saving entity to " + tableName, e);
        }
    }

    @Override
    public T update(T entity) {
        try (PreparedStatement stmt = connection.prepareStatement(getUpdateQuery())) {
            setStatementParameters(stmt, entity);
            stmt.setLong(stmt.getParameterMetaData().getParameterCount(), entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating entity in " + tableName, e);
        }
        return null;
    }

    @Override
    public Optional<T> findById(Long id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding entity by ID from " + tableName, e);
        }
    }

    @Override
    public List<T> findAll() {
        List<T> entities = new ArrayList<T>();
        String query = "SELECT * FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                entities.add(mapResultSetToEntity(rs));
            }
            return entities;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all entities from " + tableName, e);
        }
    }

    @Override
    public void delete(T entity) {
        if (entity.getId() != null) {
            deleteById(entity.getId());
        }
    }

    @Override
    public void deleteById(Long id) {
        String query = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting entity from " + tableName, e);
        }
    }

    @Override
    public boolean exists(Long id) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking entity existence in " + tableName, e);
        }
    }
}

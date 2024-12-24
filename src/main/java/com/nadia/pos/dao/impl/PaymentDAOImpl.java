package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.PaymentDAO;
import com.nadia.pos.model.Payment;
import com.nadia.pos.enums.PaymentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAOImpl<T extends Payment> extends BaseDAOImpl<T> implements PaymentDAO<T> {
    private static final String TABLE_NAME = "payments";

    public PaymentDAOImpl() throws SQLException {
        super(TABLE_NAME);
    }

    @Override
    protected T mapResultSetToEntity(ResultSet rs) throws SQLException {
        return null;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, T entity) throws SQLException {

    }

    @Override
    protected String getInsertQuery() {
        return "";
    }

    @Override
    protected String getUpdateQuery() {
        return "";
    }

    @Override
    public List<T> findByStatus(PaymentStatus status) {
        List<T> payments = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by status", e);
        }
    }

    @Override
    public List<T> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<T> payments = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE created_at BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by date range", e);
        }
    }

    @Override
    public List<T> findByEmployee(Long employeeId) {
        List<T> payments = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by employee", e);
        }
    }

    @Override
    public Optional<T> findByReference(String referenceNumber) {
        String query = "SELECT * FROM " + tableName + " WHERE reference_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, referenceNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payment by reference", e);
        }
    }
}
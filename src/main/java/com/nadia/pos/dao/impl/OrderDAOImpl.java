package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.OrderDAO;
import com.nadia.pos.model.Order;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.enums.OrderStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class OrderDAOImpl<T extends Order> extends BaseDAOImpl<T> implements OrderDAO<T> {

    protected OrderDAOImpl(String tableName) throws SQLException {
        super(tableName);
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, T order) throws SQLException {
        stmt.setString(1, order.getOrderNumber());
        stmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
        stmt.setBigDecimal(3, order.getTotalAmount());
        stmt.setBigDecimal(4, order.getTax());
        stmt.setBigDecimal(5, order.getDiscount());
        stmt.setString(6, order.getNotes());
        stmt.setString(7, order.getStatus());
        stmt.setLong(8, order.getCreatedBy().getId());
        stmt.setTimestamp(9, Timestamp.valueOf(order.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO " + tableName +
                " (order_number, order_date, total_amount, tax, discount, notes, status, created_by, " +
                "updated_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE " + tableName +
                " SET order_number=?, order_date=?, total_amount=?, tax=?, discount=?, notes=?, " +
                "status=?, created_by=?, updated_at=? WHERE id=?";
    }

    @Override
    public Optional<T> findByOrderNumber(String orderNumber) {
        String query = "SELECT * FROM " + tableName + " WHERE order_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, orderNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order by number", e);
        }
    }

    @Override
    public List<T> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<T> orders = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE order_date BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by date range", e);
        }
    }

    @Override
    public List<T> findByStatus(OrderStatus status) {
        List<T> orders = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by status", e);
        }
    }

    @Override
    public List<T> findByEmployee(Long employeeId) {
        List<T> orders = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE created_by = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by employee", e);
        }
    }

    protected void saveOrderItems(Long orderId, List<OrderItem> items) {
        String query = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, " +
                "subtotal, discount) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (OrderItem item : items) {
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.getProduct().getId());
                stmt.setLong(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPrice());
                stmt.setBigDecimal(5, item.getSubtotal());
                stmt.setBigDecimal(6, item.getDiscount());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving order items", e);
        }
    }

    protected List<OrderItem> loadOrderItems(Long orderId) {
        List<OrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToOrderItem(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading order items", e);
        }
    }

    protected abstract OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException;
}
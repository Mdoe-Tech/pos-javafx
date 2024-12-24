package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.OrderItemDAO;
import com.nadia.pos.model.OrderItem;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAOImpl<T extends OrderItem> extends BaseDAOImpl<T> implements OrderItemDAO<T> {
    private static final String TABLE_NAME = "order_items";

    public OrderItemDAOImpl() throws SQLException {
        super(TABLE_NAME);
    }

    @Override
    protected T mapResultSetToEntity(ResultSet rs) throws SQLException {
        return null;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, T item) throws SQLException {
        stmt.setLong(1, item.getProduct().getId());
        stmt.setInt(2, item.getQuantity());
        stmt.setBigDecimal(3, item.getUnitPrice());
        stmt.setBigDecimal(4, item.getDiscount());
        stmt.setString(5, item.getNotes());
        stmt.setTimestamp(6, Timestamp.valueOf(item.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO order_items (product_id, quantity, unit_price, discount, notes, " +
                "updated_at, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE order_items SET product_id=?, quantity=?, unit_price=?, discount=?, " +
                "notes=?, updated_at=? WHERE id=?";
    }

    @Override
    public List<T> findByOrder(Long orderId) {
        List<T> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order items by order", e);
        }
    }

    @Override
    public List<T> findByProduct(Long productId) {
        List<T> items = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order items by product", e);
        }
    }
}

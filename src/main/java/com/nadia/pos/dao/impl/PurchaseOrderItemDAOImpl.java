package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.PurchaseOrderItemDAO;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.PurchaseOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderItemDAOImpl extends BaseDAOImpl<PurchaseOrderItem> implements PurchaseOrderItemDAO {

    public PurchaseOrderItemDAOImpl() throws SQLException {
        super("purchase_order_items");
    }

    @Override
    protected PurchaseOrderItem mapResultSetToEntity(ResultSet rs) throws SQLException {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(rs.getLong("id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setDiscount(rs.getBigDecimal("discount"));
        item.setReceived(rs.getBoolean("received"));
        item.setReceivedQuantity(rs.getInt("received_quantity"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return item;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, PurchaseOrderItem item) throws SQLException {
        stmt.setInt(3, item.getQuantity());
        stmt.setBigDecimal(4, item.getUnitPrice());
        stmt.setBigDecimal(5, item.getDiscount());
        stmt.setBoolean(6, item.getReceived());
        stmt.setInt(7, item.getReceivedQuantity());
        stmt.setTimestamp(8, Timestamp.valueOf(item.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_price, discount, " +
                "received, received_quantity, updated_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE purchase_order_items SET purchase_order_id=?, product_id=?, quantity=?, unit_price=?, discount=?, " +
                "received=?, received_quantity=?, updated_at=? WHERE id=?";
    }

    @Override
    public List<OrderItem> findByPurchaseOrder(Long purchaseOrderId) {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM purchase_order_items WHERE purchase_order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, purchaseOrderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding items by purchase order", e);
        }
    }

    @Override
    public List<PurchaseOrderItem> findUnreceivedItems(Long purchaseOrderId) {
        List<PurchaseOrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM purchase_order_items WHERE purchase_order_id = ? AND " +
                "(received = FALSE OR received_quantity < quantity)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, purchaseOrderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding unreceived items", e);
        }
    }

    @Override
    public boolean updateReceivedQuantity(Long itemId, Integer quantity) {
        String query = "UPDATE purchase_order_items SET received_quantity = ?, " +
                "received = CASE WHEN ? >= quantity THEN TRUE ELSE FALSE END, " +
                "updated_at = NOW() WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setLong(3, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating received quantity", e);
        }
    }
}

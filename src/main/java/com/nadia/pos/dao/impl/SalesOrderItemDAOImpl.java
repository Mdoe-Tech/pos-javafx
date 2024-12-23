package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.SalesOrderItemDAO;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.SalesOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderItemDAOImpl extends BaseDAOImpl<SalesOrderItem> implements SalesOrderItemDAO {

    public SalesOrderItemDAOImpl() throws SQLException {
        super("sales_order_items");
    }

    @Override
    protected SalesOrderItem mapResultSetToEntity(ResultSet rs) throws SQLException {
        SalesOrderItem item = new SalesOrderItem();
        item.setId(rs.getLong("id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setDiscount(rs.getBigDecimal("discount"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return item;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, SalesOrderItem item) throws SQLException {
        stmt.setInt(3, item.getQuantity());
        stmt.setBigDecimal(4, item.getUnitPrice());
        stmt.setBigDecimal(5, item.getDiscount());
        stmt.setTimestamp(6, Timestamp.valueOf(item.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO sales_order_items (sales_order_id, product_id, quantity, unit_price, discount, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE sales_order_items SET sales_order_id=?, product_id=?, quantity=?, unit_price=?, discount=?, " +
                "updated_at=? WHERE id=?";
    }

    @Override
    public List<OrderItem> findBySalesOrder(Long salesOrderId) {
        List<SalesOrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM sales_order_items WHERE sales_order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, salesOrderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding items by sales order", e);
        }
    }

    @Override
    public List<SalesOrderItem> findByProduct(Long productId) {
        List<SalesOrderItem> items = new ArrayList<>();
        String query = "SELECT * FROM sales_order_items WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToEntity(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding items by product", e);
        }
    }
}
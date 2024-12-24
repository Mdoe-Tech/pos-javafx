package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.SalesOrderItemDAO;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.Product;
import com.nadia.pos.model.SalesOrderItem;
import com.nadia.pos.service.ProductService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderItemDAOImpl extends BaseDAOImpl<SalesOrderItem> implements SalesOrderItemDAO {
    private final ProductService productService;

    public SalesOrderItemDAOImpl(ProductService productService) throws SQLException {
        super("sales_order_items");
        this.productService = productService;
    }

    @Override
    protected SalesOrderItem mapResultSetToEntity(ResultSet rs) throws SQLException {
        SalesOrderItem item = new SalesOrderItem();
        item.setId(rs.getLong("id"));
        item.setSalesOrderId(rs.getLong("sales_order_id"));

        // Get product from product service
        Long productId = rs.getLong("product_id");
        Product product = productService.findProductById(productId)
                .orElseThrow(() -> new SQLException("Product not found with ID: " + productId));
        item.setProduct(product);

        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setDiscount(rs.getBigDecimal("discount"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return item;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, SalesOrderItem item) throws SQLException {
        int paramIndex = 1;

        // Validate required fields
        if (item.getSalesOrderId() == null) {
            throw new SQLException("Sales order ID cannot be null");
        }
        if (item.getProduct() == null || item.getProduct().getId() == null) {
            throw new SQLException("Product ID cannot be null");
        }

        // Set parameters in correct order
        stmt.setLong(paramIndex++, item.getSalesOrderId());
        stmt.setLong(paramIndex++, item.getProduct().getId());
        stmt.setInt(paramIndex++, item.getQuantity());

        // Handle null values for numeric fields
        stmt.setBigDecimal(paramIndex++,
                item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
        stmt.setBigDecimal(paramIndex++,
                item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);

        // Set updated_at timestamp
        stmt.setTimestamp(paramIndex++, Timestamp.valueOf(LocalDateTime.now()));

        // For UPDATE query only
        if (item.getId() != null) {
            stmt.setLong(paramIndex, item.getId());
        }
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO sales_order_items " +
                "(sales_order_id, product_id, quantity, unit_price, discount, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE sales_order_items SET " +
                "sales_order_id=?, product_id=?, quantity=?, unit_price=?, discount=?, updated_at=? " +
                "WHERE id=?";
    }

    @Override
    public List<OrderItem> findBySalesOrder(Long salesOrderId) {
        List<OrderItem> items = new ArrayList<>();
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
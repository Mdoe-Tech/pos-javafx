package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.PurchaseOrderDAO;
import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.model.PurchaseOrder;
import com.nadia.pos.model.Supplier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderDAOImpl extends BaseDAOImpl<PurchaseOrder> implements PurchaseOrderDAO {

    public PurchaseOrderDAOImpl() throws SQLException {
        super("purchase_orders");
    }

    @Override
    protected PurchaseOrder mapResultSetToEntity(ResultSet rs) throws SQLException {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(rs.getLong("id"));
        order.setSupplier(getSupplier(rs.getLong("supplier_id")));
        order.setExpectedDeliveryDate(rs.getTimestamp("expected_delivery_date").toLocalDateTime());
        order.setShippingTerms(rs.getString("shipping_terms"));
        order.setPaymentTerms(rs.getString("payment_terms"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return order;
    }

    private Supplier getSupplier(Long supplierId) {
        return new Supplier();
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, PurchaseOrder order) throws SQLException {
        stmt.setLong(1, order.getSupplier().getId());
        stmt.setTimestamp(2, Timestamp.valueOf(order.getExpectedDeliveryDate()));
        stmt.setString(3, order.getShippingTerms());
        stmt.setString(4, order.getPaymentTerms());
        stmt.setString(5, order.getStatus());
        stmt.setBigDecimal(6, order.getTotalAmount());
        stmt.setBigDecimal(7, order.getTax());
        stmt.setBigDecimal(8, order.getDiscount());
        stmt.setTimestamp(9, Timestamp.valueOf(order.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO purchase_orders (supplier_id, expected_delivery_date, shipping_terms, payment_terms, " +
                "status, total_amount, tax, discount, updated_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE purchase_orders SET supplier_id=?, expected_delivery_date=?, shipping_terms=?, payment_terms=?, " +
                "status=?, total_amount=?, tax=?, discount=?, updated_at=? WHERE id=?";
    }

    @Override
    public List<PurchaseOrder> findBySupplier(Long supplierId) {
        List<PurchaseOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM purchase_orders WHERE supplier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, supplierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding purchase orders by supplier", e);
        }
    }

    @Override
    public List<PurchaseOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<PurchaseOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM purchase_orders WHERE created_at BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding purchase orders by date range", e);
        }
    }

    @Override
    public List<PurchaseOrder> findPendingDeliveries() {
        List<PurchaseOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM purchase_orders WHERE status = 'PENDING' AND expected_delivery_date <= NOW()";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending deliveries", e);
        }
    }

    @Override
    public List<PurchaseOrder> findByStatus(String status) {
        List<PurchaseOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM purchase_orders WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding purchase orders by status", e);
        }
    }
}

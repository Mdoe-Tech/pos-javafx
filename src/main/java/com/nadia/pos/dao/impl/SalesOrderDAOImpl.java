package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.SalesOrderDAO;
import com.nadia.pos.model.Customer;
import com.nadia.pos.model.SalesOrder;
import com.nadia.pos.enums.SalesType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesOrderDAOImpl extends BaseDAOImpl<SalesOrder> implements SalesOrderDAO {

    public SalesOrderDAOImpl() throws SQLException {
        super("sales_orders");
    }

    @Override
    protected SalesOrder mapResultSetToEntity(ResultSet rs) throws SQLException {
        SalesOrder order = new SalesOrder();
        order.setId(rs.getLong("id"));
        order.setCustomer(getCustomer(rs.getLong("customer_id")));
        order.setType(SalesType.valueOf(rs.getString("type")));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setDeliveryDate(rs.getTimestamp("delivery_date").toLocalDateTime());
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return order;
    }

    private Customer getCustomer(Long customerId) {
        // Implementation to fetch customer details
        return new Customer();
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, SalesOrder order) throws SQLException {
        stmt.setLong(1, order.getCustomer().getId());
        stmt.setString(2, order.getType().name());
        stmt.setString(3, order.getDeliveryAddress());
        stmt.setTimestamp(4, Timestamp.valueOf(order.getDeliveryDate()));
        stmt.setBigDecimal(5, order.getTotalAmount());
        stmt.setBigDecimal(6, order.getTax());
        stmt.setBigDecimal(7, order.getDiscount());
        stmt.setTimestamp(8, Timestamp.valueOf(order.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO sales_orders (customer_id, type, delivery_address, delivery_date, " +
                "total_amount, tax, discount, updated_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE sales_orders SET customer_id=?, type=?, delivery_address=?, delivery_date=?, " +
                "total_amount=?, tax=?, discount=?, updated_at=? WHERE id=?";
    }

    @Override
    public List<SalesOrder> findByCustomer(Long customerId) {
        List<SalesOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM sales_orders WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sales orders by customer", e);
        }
    }

    @Override
    public List<SalesOrder> findByType(SalesType type) {
        List<SalesOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM sales_orders WHERE sales_type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sales orders by type", e);
        }
    }

    @Override
    public List<SalesOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<SalesOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM sales_orders WHERE created_at BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToEntity(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sales orders by date range", e);
        }
    }

    @Override
    public List<SalesOrder> findPendingDeliveries() {
        List<SalesOrder> orders = new ArrayList<>();
        String query = "SELECT * FROM sales_orders WHERE delivery_date <= NOW() AND status = 'PENDING'";
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
}

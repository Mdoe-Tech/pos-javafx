package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.SalesOrderDAO;
import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.model.Customer;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.SalesOrder;
import com.nadia.pos.enums.SalesType;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.service.EmployeeService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderDAOImpl extends BaseDAOImpl<SalesOrder> implements SalesOrderDAO {
    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public SalesOrderDAOImpl(CustomerService customerService, EmployeeService employeeService) throws SQLException {
        super("sales_orders");
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    @Override
    protected SalesOrder mapResultSetToEntity(ResultSet rs) throws SQLException {
        SalesOrder order = new SalesOrder();
        order.setId(rs.getLong("id"));
        order.setCustomer(getCustomer(rs.getLong("customer_id")));
        order.setOrderNumber(rs.getString("order_number"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setType(SalesType.valueOf(rs.getString("type")));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setDeliveryDate(rs.getTimestamp("delivery_date").toLocalDateTime());
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setTax(rs.getBigDecimal("tax"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        order.setCreatedBy(getEmployee(rs.getLong("created_by")));

        return order;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, SalesOrder order) throws SQLException {
        int paramIndex = 1;
        stmt.setLong(paramIndex++, order.getCustomer().getId());
        stmt.setString(paramIndex++, order.getOrderNumber());
        stmt.setTimestamp(paramIndex++, Timestamp.valueOf(order.getOrderDate()));
        stmt.setString(paramIndex++, order.getType().name());
        stmt.setString(paramIndex++, order.getDeliveryAddress());
        stmt.setTimestamp(paramIndex++, Timestamp.valueOf(order.getDeliveryDate()));
        stmt.setBigDecimal(paramIndex++, order.getTotalAmount());
        stmt.setBigDecimal(paramIndex++, order.getTax());
        stmt.setBigDecimal(paramIndex++, order.getDiscount());
        stmt.setString(paramIndex++, order.getStatus().name());
        stmt.setString(paramIndex++, order.getNotes());
        stmt.setTimestamp(paramIndex++, Timestamp.valueOf(LocalDateTime.now()));
        stmt.setLong(paramIndex++, order.getCreatedBy().getId());

        if (order.getId() != null) {
            stmt.setLong(paramIndex, order.getId());
        }
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO sales_orders (" +
                "customer_id, order_number, order_date, type, delivery_address, " +
                "delivery_date, total_amount, tax, discount, status, notes, " +
                "updated_at, created_by, created_at) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE sales_orders SET " +
                "customer_id=?, order_number=?, order_date=?, type=?, " +
                "delivery_address=?, delivery_date=?, total_amount=?, tax=?, " +
                "discount=?, status=?, notes=?, updated_at=?, created_by=? " +
                "WHERE id=?";
    }

    private Customer getCustomer(Long customerId) {
        if (customerId == null) {
            return null;
        }

        Optional<Customer> customer = customerService.findCustomerById(customerId);
        if (customer.isEmpty()) {
            throw new RuntimeException("Customer not found with ID: " + customerId);
        }
        return customer.get();
    }

    private Employee getEmployee(Long employeeId) {
        if (employeeId == null) {
            return null;
        }

        Optional<Employee> employee = employeeService.findEmployeeById(employeeId);
        if (employee.isEmpty()) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }
        return employee.get();
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
        String query = "SELECT * FROM sales_orders WHERE type = ?";
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

package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.CustomerDAO;
import com.nadia.pos.model.Customer;
import com.nadia.pos.enums.CustomerType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl extends BaseDAOImpl<Customer> implements CustomerDAO {

    public CustomerDAOImpl() throws SQLException {
        super("customers");
    }

    @Override
    protected Customer mapResultSetToEntity(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setCode(rs.getString("code"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        customer.setType(CustomerType.valueOf(rs.getString("type")));
        customer.setCreditLimit(rs.getBigDecimal("credit_limit"));
        customer.setCurrentCredit(rs.getBigDecimal("current_credit"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        customer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return customer;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, Customer customer) throws SQLException {
        stmt.setString(1, customer.getName());
        stmt.setString(2, customer.getCode());
        stmt.setString(3, customer.getPhone());
        stmt.setString(4, customer.getEmail());
        stmt.setString(5, customer.getAddress());
        stmt.setString(6, customer.getType().name());
        stmt.setBigDecimal(7, customer.getCreditLimit());
        stmt.setBigDecimal(8, customer.getCurrentCredit());
        stmt.setTimestamp(9, Timestamp.valueOf(customer.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO customers (name, code, phone, email, address, type, credit_limit, current_credit, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE customers SET name=?, code=?, phone=?, email=?, address=?, type=?, credit_limit=?, current_credit=?, " +
                "updated_at=? WHERE id=?";
    }

    @Override
    public Optional<Customer> findByCode(String code) {
        String query = "SELECT * FROM customers WHERE code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by code", e);
        }
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        String query = "SELECT * FROM customers WHERE phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by phone", e);
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String query = "SELECT * FROM customers WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by email", e);
        }
    }

    @Override
    public List<Customer> findByType(CustomerType type) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, type.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToEntity(rs));
            }
            return customers;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customers by type", e);
        }
    }

    @Override
    public List<Customer> searchByName(String namePattern) {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers WHERE name LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + namePattern + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToEntity(rs));
            }
            return customers;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching customers by name", e);
        }
    }

    @Override
    public boolean updateCreditBalance(Long customerId, BigDecimal amount) {
        String query = "UPDATE customers SET current_credit = current_credit + ?, updated_at = NOW() WHERE id = ? " +
                "AND (current_credit + ?) <= credit_limit";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBigDecimal(1, amount);
            stmt.setLong(2, customerId);
            stmt.setBigDecimal(3, amount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating customer credit balance", e);
        }
    }

    @Override
    public List<Customer> findCustomersExceedingCredit() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers WHERE current_credit > credit_limit";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                customers.add(mapResultSetToEntity(rs));
            }
            return customers;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customers exceeding credit", e);
        }
    }
}
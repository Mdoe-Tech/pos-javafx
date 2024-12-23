package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.model.Employee;
import com.nadia.pos.enums.EmployeeStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAOImpl extends BaseDAOImpl<Employee> implements EmployeeDAO {

    public EmployeeDAOImpl() throws SQLException {
        super("employees");
    }

    @Override
    protected Employee mapResultSetToEntity(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setEmployeeId(rs.getString("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));
        employee.setDateOfBirth(rs.getDate("date_of_birth") != null ?
                rs.getDate("date_of_birth").toLocalDate() : null);
        employee.setHireDate(rs.getDate("hire_date").toLocalDate());
        employee.setDepartment(rs.getString("department"));
        employee.setPosition(rs.getString("position"));
        employee.setStatus(EmployeeStatus.valueOf(rs.getString("status")));
        employee.setUsername(rs.getString("username"));
        employee.setPassword(rs.getString("password"));
        employee.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        employee.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return employee;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, Employee employee) throws SQLException {
        stmt.setString(1, employee.getEmployeeId());
        stmt.setString(2, employee.getFirstName());
        stmt.setString(3, employee.getLastName());
        stmt.setString(4, employee.getEmail());
        stmt.setString(5, employee.getPhone());
        stmt.setString(6, employee.getAddress());
        stmt.setDate(7, employee.getDateOfBirth() != null ?
                Date.valueOf(employee.getDateOfBirth()) : null);
        stmt.setDate(8, Date.valueOf(employee.getHireDate()));
        stmt.setString(9, employee.getDepartment());
        stmt.setString(10, employee.getPosition());
        stmt.setString(11, employee.getStatus().name());
        stmt.setString(12, employee.getUsername());
        stmt.setString(13, employee.getPassword());
        stmt.setTimestamp(14, Timestamp.valueOf(employee.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO employees (employee_id, first_name, last_name, email, phone, address, " +
                "date_of_birth, hire_date, department, position, status, username, password, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE employees SET employee_id=?, first_name=?, last_name=?, email=?, phone=?, " +
                "address=?, date_of_birth=?, hire_date=?, department=?, position=?, status=?, " +
                "username=?, password=?, updated_at=? WHERE id=?";
    }

    @Override
    public Optional<Employee> findByEmployeeId(String employeeId) {
        String query = "SELECT * FROM employees WHERE employee_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding employee by ID", e);
        }
    }

    @Override
    public Optional<Employee> findByUsername(String username) {
        String query = "SELECT * FROM employees WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding employee by username", e);
        }
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        String query = "SELECT * FROM employees WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding employee by email", e);
        }
    }

    @Override
    public List<Employee> findByDepartment(String department) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM employees WHERE department = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, department);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                employees.add(mapResultSetToEntity(rs));
            }
            return employees;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding employees by department", e);
        }
    }
}
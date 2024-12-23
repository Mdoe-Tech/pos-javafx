package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.SupplierDAO;
import com.nadia.pos.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDAOImpl extends BaseDAOImpl<Supplier> implements SupplierDAO {

    public SupplierDAOImpl() throws SQLException {
        super("suppliers");
    }

    @Override
    protected Supplier mapResultSetToEntity(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setId(rs.getLong("id"));
        supplier.setName(rs.getString("name"));
        supplier.setCode(rs.getString("code"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        supplier.setTaxId(rs.getString("tax_id"));
        supplier.setBankAccount(rs.getString("bank_account"));
        supplier.setNotes(rs.getString("notes"));
        supplier.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        supplier.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return supplier;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, Supplier supplier) throws SQLException {
        stmt.setString(1, supplier.getName());
        stmt.setString(2, supplier.getCode());
        stmt.setString(3, supplier.getContactPerson());
        stmt.setString(4, supplier.getPhone());
        stmt.setString(5, supplier.getEmail());
        stmt.setString(6, supplier.getAddress());
        stmt.setString(7, supplier.getTaxId());
        stmt.setString(8, supplier.getBankAccount());
        stmt.setString(9, supplier.getNotes());
        stmt.setTimestamp(10, Timestamp.valueOf(supplier.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO suppliers (name, code, contact_person, phone, email, address, tax_id, bank_account, notes, " +
                "updated_at, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE suppliers SET name=?, code=?, contact_person=?, phone=?, email=?, address=?, tax_id=?, " +
                "bank_account=?, notes=?, updated_at=? WHERE id=?";
    }

    @Override
    public Optional<Supplier> findByCode(String code) {
        String query = "SELECT * FROM suppliers WHERE code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding supplier by code", e);
        }
    }

    @Override
    public Optional<Supplier> findByPhone(String phone) {
        String query = "SELECT * FROM suppliers WHERE phone = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding supplier by phone", e);
        }
    }

    @Override
    public Optional<Supplier> findByEmail(String email) {
        String query = "SELECT * FROM suppliers WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding supplier by email", e);
        }
    }

    @Override
    public List<Supplier> searchByName(String namePattern) {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM suppliers WHERE name LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + namePattern + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(mapResultSetToEntity(rs));
            }
            return suppliers;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching suppliers by name", e);
        }
    }
}
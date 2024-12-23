package com.nadia.pos.service;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Supplier;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SupplierService {
    Supplier createSupplier(Supplier supplier) throws ValidationException, SQLException;
    Supplier updateSupplier(Long id, Supplier supplier) throws ValidationException, SQLException;
    void deleteSupplier(Supplier supplier) throws SQLException;
    Optional<Supplier> getSupplier(Long id) throws SQLException;
    List<Supplier> getAllSuppliers() throws SQLException;
    Optional<Supplier> findSupplierByCode(String code) throws SQLException;
    Optional<Supplier> findSupplierByPhone(String phone) throws SQLException;
    Optional<Supplier> findSupplierByEmail(String email) throws SQLException;
    List<Supplier> searchSuppliersByName(String namePattern) throws SQLException;
}
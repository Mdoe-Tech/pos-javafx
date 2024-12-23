package com.nadia.pos.service.impl;

import com.nadia.pos.dao.SupplierDAO;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Supplier;
import com.nadia.pos.service.SupplierService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SupplierServiceImpl implements SupplierService {
    private final SupplierDAO supplierDAO;

    public SupplierServiceImpl(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    @Override
    public Supplier createSupplier(Supplier supplier) throws ValidationException, SQLException {
        // Validate supplier data
        supplier.validate();

        // Check for duplicate code if provided
        if (supplier.getCode() != null && !supplier.getCode().trim().isEmpty()) {
            Optional<Supplier> existingSupplier = supplierDAO.findByCode(supplier.getCode());
            if (existingSupplier.isPresent()) {
                throw new ValidationException("Supplier with code " + supplier.getCode() + " already exists");
            }
        }

        // Check for duplicate phone
        Optional<Supplier> supplierWithPhone = supplierDAO.findByPhone(supplier.getPhone());
        if (supplierWithPhone.isPresent()) {
            throw new ValidationException("Supplier with phone " + supplier.getPhone() + " already exists");
        }

        // Check for duplicate email if provided
        if (supplier.getEmail() != null && !supplier.getEmail().trim().isEmpty()) {
            Optional<Supplier> supplierWithEmail = supplierDAO.findByEmail(supplier.getEmail());
            if (supplierWithEmail.isPresent()) {
                throw new ValidationException("Supplier with email " + supplier.getEmail() + " already exists");
            }
        }

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierDAO.save(supplier);
    }

    @Override
    public Supplier updateSupplier(Long id, Supplier supplier) throws ValidationException, SQLException {
        // Validate supplier data
        supplier.validate();

        // Check if supplier exists
        Optional<Supplier> existingSupplier = supplierDAO.findById(id);
        if (existingSupplier.isEmpty()) {
            throw new ValidationException("Supplier with id " + id + " not found");
        }

        // Check for duplicate code if changed
        if (supplier.getCode() != null && !supplier.getCode().equals(existingSupplier.get().getCode())) {
            Optional<Supplier> supplierWithCode = supplierDAO.findByCode(supplier.getCode());
            if (supplierWithCode.isPresent()) {
                throw new ValidationException("Supplier with code " + supplier.getCode() + " already exists");
            }
        }

        // Check for duplicate phone if changed
        if (!supplier.getPhone().equals(existingSupplier.get().getPhone())) {
            Optional<Supplier> supplierWithPhone = supplierDAO.findByPhone(supplier.getPhone());
            if (supplierWithPhone.isPresent()) {
                throw new ValidationException("Supplier with phone " + supplier.getPhone() + " already exists");
            }
        }

        // Check for duplicate email if changed
        if (supplier.getEmail() != null && !supplier.getEmail().equals(existingSupplier.get().getEmail())) {
            Optional<Supplier> supplierWithEmail = supplierDAO.findByEmail(supplier.getEmail());
            if (supplierWithEmail.isPresent()) {
                throw new ValidationException("Supplier with email " + supplier.getEmail() + " already exists");
            }
        }

        // Set ID and timestamps
        supplier.setId(id);
        supplier.setCreatedAt(existingSupplier.get().getCreatedAt());
        supplier.setUpdatedAt(LocalDateTime.now());

        return supplierDAO.update(supplier);
    }

    @Override
    public void deleteSupplier(Supplier supplier) throws SQLException {
        supplierDAO.delete(supplier);
    }

    @Override
    public Optional<Supplier> getSupplier(Long id) throws SQLException {
        return supplierDAO.findById(id);
    }

    @Override
    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.findAll();
    }

    @Override
    public Optional<Supplier> findSupplierByCode(String code) throws SQLException {
        return supplierDAO.findByCode(code);
    }

    @Override
    public Optional<Supplier> findSupplierByPhone(String phone) throws SQLException {
        return supplierDAO.findByPhone(phone);
    }

    @Override
    public Optional<Supplier> findSupplierByEmail(String email) throws SQLException {
        return supplierDAO.findByEmail(email);
    }

    @Override
    public List<Supplier> searchSuppliersByName(String namePattern) throws SQLException {
        return supplierDAO.searchByName(namePattern);
    }
}
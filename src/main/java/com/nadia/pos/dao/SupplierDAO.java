package com.nadia.pos.dao;

import com.nadia.pos.model.Supplier;
import java.util.List;
import java.util.Optional;

public interface SupplierDAO extends BaseDAO<Supplier> {
    Optional<Supplier> findByCode(String code);
    Optional<Supplier> findByPhone(String phone);
    Optional<Supplier> findByEmail(String email);
    List<Supplier> searchByName(String namePattern);
}

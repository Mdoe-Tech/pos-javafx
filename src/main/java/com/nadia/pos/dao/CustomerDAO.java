package com.nadia.pos.dao;

import com.nadia.pos.model.Customer;
import com.nadia.pos.enums.CustomerType;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO extends BaseDAO<Customer> {
    Optional<Customer> findByCode(String code);
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByType(CustomerType type);
    List<Customer> searchByName(String namePattern);
    List<Customer> findCustomersExceedingCredit();
}
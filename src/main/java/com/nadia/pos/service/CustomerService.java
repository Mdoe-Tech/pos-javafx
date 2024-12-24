package com.nadia.pos.service;

import com.nadia.pos.model.Customer;
import com.nadia.pos.enums.CustomerType;
import com.nadia.pos.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    void createCustomer(Customer customer) throws ValidationException;

    void updateCustomer(Customer customer) throws ValidationException;

    void deleteCustomer(Customer id) throws ValidationException;

    Optional<Customer> findCustomerById(Long id);

    Optional<Customer> findCustomerByCode(String code);

    List<Customer> searchCustomers(String namePattern);

    List<Customer> findCustomersByType(CustomerType type);

    List<Customer> findCustomersExceedingCredit();
}
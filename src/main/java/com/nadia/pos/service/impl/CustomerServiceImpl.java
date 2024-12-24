package com.nadia.pos.service.impl;

import com.nadia.pos.dao.CustomerDAO;
import com.nadia.pos.model.Customer;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.enums.CustomerType;
import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerServiceImpl(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    @Override
    public void createCustomer(Customer customer) throws ValidationException {
        // Validate customer data
        customer.validate();

        // Check for duplicate code, phone, or email
        validateUniqueness(customer);

        // Set default values if not provided
        if (customer.getType() == null) {
            customer.setType(CustomerType.RETAIL);
        }
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(BigDecimal.ZERO);
        }
        if (customer.getCurrentCredit() == null) {
            customer.setCurrentCredit(BigDecimal.ZERO);
        }

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);

        customerDAO.save(customer);
    }

    @Override
    public void updateCustomer(Customer customer) throws ValidationException {
        // Validate customer exists
        customerDAO.findById(customer.getId())
                .orElseThrow(() -> new ValidationException("Customer not found"));

        // Validate customer data
        customer.validate();

        // Check for duplicate code, phone, or email (excluding current customer)
        validateUniquenessForUpdate(customer);

        // Update timestamp
        customer.setUpdatedAt(LocalDateTime.now());

        customerDAO.update(customer);
    }

    @Override
    public void deleteCustomer(Customer c) throws ValidationException {
        Customer customer = customerDAO.findById(c.getId())
                .orElseThrow(() -> new ValidationException("Customer not found"));

        if (!customer.getOrders().isEmpty()) {
            throw new ValidationException("Cannot delete customer with existing orders");
        }

        customerDAO.delete(c);
    }

    @Override
    public Optional<Customer> findCustomerById(Long id) {
        return customerDAO.findById(id);
    }

    @Override
    public Optional<Customer> findCustomerByCode(String code) {
        return customerDAO.findByCode(code);
    }

    @Override
    public List<Customer> searchCustomers(String namePattern) {
        return customerDAO.searchByName(namePattern);
    }

    @Override
    public List<Customer> findCustomersByType(CustomerType type) {
        return customerDAO.findByType(type);
    }

    @Override
    public List<Customer> findCustomersExceedingCredit() {
        return customerDAO.findCustomersExceedingCredit();
    }

    private void validateUniqueness(Customer customer) throws ValidationException {
        // Check unique code
        if (customer.getCode() != null && customerDAO.findByCode(customer.getCode()).isPresent()) {
            throw new ValidationException("Customer code already exists");
        }

        // Check unique phone
        if (customer.getPhone() != null && customerDAO.findByPhone(customer.getPhone()).isPresent()) {
            throw new ValidationException("Phone number already exists");
        }

        // Check unique email
        if (customer.getEmail() != null && customerDAO.findByEmail(customer.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists");
        }
    }

    private void validateUniquenessForUpdate(Customer customer) throws ValidationException {
        // Check unique code
        if (customer.getCode() != null) {
            customerDAO.findByCode(customer.getCode())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(customer.getId())) {
                            try {
                                throw new ValidationException("Customer code already exists");
                            } catch (ValidationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }

        // Check unique phone
        if (customer.getPhone() != null) {
            customerDAO.findByPhone(customer.getPhone())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(customer.getId())) {
                            try {
                                throw new ValidationException("Phone number already exists");
                            } catch (ValidationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }

        // Check unique email
        if (customer.getEmail() != null) {
            customerDAO.findByEmail(customer.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(customer.getId())) {
                            try {
                                throw new ValidationException("Email already exists");
                            } catch (ValidationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }
    }
}
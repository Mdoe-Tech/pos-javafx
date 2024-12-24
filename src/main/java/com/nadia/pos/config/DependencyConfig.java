package com.nadia.pos.config;

import com.nadia.pos.dao.*;
import com.nadia.pos.dao.impl.*;
import com.nadia.pos.service.*;
import com.nadia.pos.service.impl.*;

import java.sql.SQLException;

public class DependencyConfig {
    private static DependencyConfig instance;

    private final CustomerService customerService;

    private DependencyConfig() throws SQLException {
        CustomerDAO customerDAO = new CustomerDAOImpl();
        customerService = new CustomerServiceImpl(customerDAO);
    }

    public static synchronized DependencyConfig getInstance() throws SQLException {
        if (instance == null) {
            instance = new DependencyConfig();
        }
        return instance;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }
}
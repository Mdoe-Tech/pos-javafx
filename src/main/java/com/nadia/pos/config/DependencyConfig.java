package com.nadia.pos.config;

import com.nadia.pos.dao.*;
import com.nadia.pos.dao.impl.*;
import com.nadia.pos.service.*;
import com.nadia.pos.service.impl.*;
import com.nadia.pos.model.Order;
import com.nadia.pos.model.OrderItem;

import java.sql.SQLException;

public class DependencyConfig {
    private static DependencyConfig instance;

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService<Order> orderService;
    private final SalesOrderService salesOrderService;
    private final EmployeeService employeeService;

    private DependencyConfig() throws SQLException {
        CustomerDAO customerDAO = new CustomerDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();
        OrderDAO<Order> orderDAO = new OrderDAOImpl<>();
        OrderItemDAO<OrderItem> orderItemDAO = new OrderItemDAOImpl<>();
        EmployeeDAO employeeDAO = new EmployeeDAOImpl();

        this.customerService = new CustomerServiceImpl(customerDAO);
        this.productService = new ProductServiceImpl(productDAO);
        this.employeeService = new EmployeeServiceImpl(employeeDAO);

        SalesOrderDAO salesOrderDAO = new SalesOrderDAOImpl(this.customerService, this.employeeService);
        SalesOrderItemDAO salesOrderItemDAO = new SalesOrderItemDAOImpl(this.productService);

        this.orderService = new AbstractOrderServiceImpl<Order>(orderDAO, orderItemDAO, employeeDAO, productDAO) {};
        this.salesOrderService = new SalesOrderServiceImpl(salesOrderDAO, salesOrderItemDAO);
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

    public ProductService getProductService() {
        return productService;
    }

    public OrderService<Order> getOrderService() {
        return orderService;
    }

    public SalesOrderService getSalesOrderService() {
        return salesOrderService;
    }

    public EmployeeService getEmployeeService() {
        return employeeService;
    }
}
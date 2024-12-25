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
    private final InventoryService inventoryService;
    private final StockMovementService stockMovementService;

    private DependencyConfig() throws SQLException {
        CustomerDAO customerDAO = new CustomerDAOImpl();
        ProductDAO productDAO = new ProductDAOImpl();
        OrderDAO<Order> orderDAO = new OrderDAOImpl<>();
        OrderItemDAO<OrderItem> orderItemDAO = new OrderItemDAOImpl<>();
        EmployeeDAO employeeDAO = new EmployeeDAOImpl();
        InventoryDAO inventoryDAO = new InventoryDAOImpl();
        StockMovementDAO stockMovementDAO = new StockMovementDAOImpl();

        this.employeeService = new EmployeeServiceImpl(employeeDAO);
        this.productService = new ProductServiceImpl(productDAO);
        this.customerService = new CustomerServiceImpl(customerDAO);

        this.inventoryService = new InventoryServiceImpl(inventoryDAO, productDAO,stockMovementDAO);

        SalesOrderDAO salesOrderDAO = new SalesOrderDAOImpl(customerService, employeeService);
        SalesOrderItemDAO salesOrderItemDAO = new SalesOrderItemDAOImpl(productService);

        this.orderService = new AbstractOrderServiceImpl<Order>(
                orderDAO,
                orderItemDAO,
                employeeDAO,
                productDAO
        ) {};
        this.salesOrderService = new SalesOrderServiceImpl(salesOrderDAO, salesOrderItemDAO);
        this.stockMovementService = new StockMovementServiceImpl(
                stockMovementDAO,
                inventoryDAO,
                productDAO,
                employeeDAO
        );
    }

    public static synchronized DependencyConfig getInstance() throws SQLException {
        try {
            if (instance == null) {
                instance = new DependencyConfig();
            }
            return instance;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Failed to initialize dependencies: " + e.getMessage(), e);
        }
    }

    // Getters
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

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public StockMovementService getStockMovementService() {
        return stockMovementService;
    }
}
package com.nadia.pos;

import com.nadia.pos.config.DependencyConfig;
import com.nadia.pos.controller.*;
import com.nadia.pos.model.Order;
import com.nadia.pos.service.*;
import com.nadia.pos.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class POSApplication extends Application {
    private SceneManager sceneManager;
    private DependencyConfig dependencies;

    @Override
    public void start(Stage stage) {
        try {
            dependencies = DependencyConfig.getInstance();
            sceneManager = new SceneManager(stage);

            initializeScenes();

            stage.setTitle("POS System");
            stage.setOnCloseRequest(event -> {
                event.consume();
                handleApplicationClose(stage);
            });
            stage.show();
        } catch (Exception e) {
            showErrorAndExit(e);
        }
    }

    private void initializeScenes() throws Exception {
        try {
            // Get services from dependencies
            CustomerService customerService = dependencies.getCustomerService();
            ProductService productService = dependencies.getProductService();
            EmployeeService employeeService = dependencies.getEmployeeService();
            OrderService<Order> orderService = dependencies.getOrderService();
            SalesOrderService salesOrderService = dependencies.getSalesOrderService();
            InventoryService inventoryService = dependencies.getInventoryService();
            StockMovementService stockMovementService = dependencies.getStockMovementService();

            // Initialize controllers with required services
            CustomerController customerController = new CustomerController(customerService);
            EmployeeController employeeController = new EmployeeController(employeeService);
            ProductController productController = new ProductController(productService);
            MainController mainController = new MainController(sceneManager);

            // Initialize inventory-related controllers
            InventoryController inventoryController = new InventoryController(inventoryService,productService,employeeService);
            StockMovementController stockMovementController = new StockMovementController(
                    stockMovementService,
                    productService,
                    employeeService
            );

            // Initialize order controllers
            OrderController orderController = new OrderController(
                    orderService,
                    employeeService
            );

            SalesOrderController salesOrderController = new SalesOrderController(
                    salesOrderService,
                    customerService,
                    employeeService,
                    productService
            );

            DashboardController dashboardController = new DashboardController(
                    customerService,
                    employeeService,
                    salesOrderService
            );

            // Load scenes
            sceneManager.loadScene("main", "/fxml/main-layout.fxml", mainController);
            sceneManager.loadView("employees", "/fxml/employee-view.fxml", employeeController);
            sceneManager.loadView("customers", "/fxml/customer-view.fxml", customerController);
            sceneManager.loadView("products", "/fxml/products-view.fxml", productController);
            sceneManager.loadView("orders", "/fxml/order-view.fxml", orderController);
            sceneManager.loadView("sales", "/fxml/sales-order-view.fxml", salesOrderController);
            sceneManager.loadView("inventories", "/fxml/inventory-view.fxml", inventoryController);
            sceneManager.loadView("stockMovements", "/fxml/stock-movement-view.fxml", stockMovementController);
            sceneManager.loadView("dashboard", "/fxml/dashboard-view.fxml", dashboardController);


            // Switch to main scene and load views
            sceneManager.switchScene("main");
            mainController.loadView("customers");
            mainController.loadView("products");
            mainController.loadView("orders");
            mainController.loadView("sales");
            mainController.loadView("employees");
            mainController.loadView("inventories");
            mainController.loadView("stockMovements");
            mainController.loadView("dashboard");


        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to initialize scenes: " + e.getMessage(), e);
        }
    }

    private void showErrorAndExit(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Application Error");
        alert.setHeaderText("Failed to start application");
        alert.setContentText("Error: " + e.getMessage());
        alert.showAndWait();
        System.exit(1);
    }

    private void handleApplicationClose(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            stage.close();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
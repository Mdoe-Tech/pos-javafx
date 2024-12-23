package com.nadia.pos;

import com.nadia.pos.dao.CustomerDAO;
import com.nadia.pos.dao.impl.CustomerDAOImpl;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.service.impl.CustomerServiceImpl;
import com.nadia.pos.controller.CustomerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class POSApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        // Initialize dependencies
        CustomerDAO customerDAO = new CustomerDAOImpl();
        CustomerService customerService = new CustomerServiceImpl(customerDAO);
        CustomerController controller = new CustomerController(customerService);

        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(POSApplication.class.getResource("customer-view.fxml"));
        fxmlLoader.setController(controller);

        // Set up the scene
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 800, 600);

        // Configure and show the stage
        stage.setTitle("POS System - Customer Management");
        stage.setScene(scene);
        stage.show();
    }
}
package com.nadia.pos.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import com.nadia.pos.utils.SceneManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private StackPane contentArea;
    @FXML private Button dashboardBtn;
    @FXML private Button ordersBtn;
    @FXML private Button salesBtn;
    @FXML private Button customersBtn;
    @FXML private Button productsBtn;
    @FXML private Button inventoryBtn;
    @FXML private Button settingsBtn;

    private final SceneManager sceneManager;

    public MainController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupNavigation();
    }

    private void setupNavigation() {
        dashboardBtn.setOnAction(e -> loadView("dashboard"));
        ordersBtn.setOnAction(e -> loadView("orders"));
        salesBtn.setOnAction(e -> loadView("sales"));
        customersBtn.setOnAction(e -> loadView("customers"));
        productsBtn.setOnAction(e -> loadView("products"));
        inventoryBtn.setOnAction(e -> loadView("inventory"));
        settingsBtn.setOnAction(e -> loadView("settings"));
    }

    public void loadView(String viewName) {
        try {
            contentArea.getChildren().setAll(sceneManager.getView(viewName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
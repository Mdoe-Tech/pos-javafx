package com.nadia.pos;

import com.nadia.pos.config.DependencyConfig;
import com.nadia.pos.controller.CustomerController;
import com.nadia.pos.controller.MainController;
import com.nadia.pos.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

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
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeScenes() throws Exception {
        CustomerController customerController = new CustomerController(dependencies.getCustomerService());
        MainController mainController = new MainController(sceneManager);

        sceneManager.loadScene("main", "/fxml/main-layout.fxml", mainController);
        sceneManager.loadView("customers", "/fxml/customer-view.fxml", customerController);

        sceneManager.switchScene("main");

        mainController.loadView("customers");
    }

    public static void main(String[] args) {
        launch();
    }
}
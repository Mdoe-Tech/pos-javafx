package com.nadia.pos.utils;

import com.nadia.pos.POSApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private final Stage primaryStage;
    private final Map<String, Scene> scenes = new HashMap<>();
    private final Map<String, Parent> views = new HashMap<>();
    private final Map<String, Object> controllers = new HashMap<>();

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void loadScene(String name, String fxmlPath, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(POSApplication.class.getResource(fxmlPath));
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        scenes.put(name, scene);
        controllers.put(name, controller);
    }

    public void loadView(String name, String fxmlPath, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(POSApplication.class.getResource(fxmlPath));
        loader.setController(controller);
        Parent root = loader.load();
        views.put(name, root);
        controllers.put(name, controller);
    }

    public Parent getView(String name) {
        return views.get(name);
    }

    public void switchScene(String name) {
        Scene scene = scenes.get(name);
        if (scene != null) {
            primaryStage.setScene(scene);
        }
    }

    public Object getController(String name) {
        return controllers.get(name);
    }
}
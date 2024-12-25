package com.nadia.pos.controller;

import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Inventory;
import com.nadia.pos.model.Product;
import com.nadia.pos.service.EmployeeService;
import com.nadia.pos.service.InventoryService;
import com.nadia.pos.service.ProductService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final EmployeeService employeeService;


    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, String> productColumn;
    @FXML private TableColumn<Inventory, Integer> quantityColumn;
    @FXML private TableColumn<Inventory, Integer> minStockColumn;
    @FXML private TableColumn<Inventory, Integer> maxStockColumn;
    @FXML private TableColumn<Inventory, String> locationColumn;
    @FXML private TableColumn<Inventory, String> binNumberColumn;

    @FXML private TextField productSearchField;
    @FXML private Button refreshButton;
    @FXML private Button addStockButton;
    @FXML private Button removeStockButton;
    @FXML private Button stockCheckButton;
    @FXML private Button editButton;
    @FXML private Button createButton;

    private FilteredList<Inventory> filteredData;

    public InventoryController(InventoryService inventoryService, ProductService productService, EmployeeService employeeService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.employeeService = employeeService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupSearch();
        loadInventoryData();
        setupEventHandlers();
        setupContextMenu();
    }

    private void setupTableColumns() {
        productColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct().getName()));
        quantityColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        minStockColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getMinimumStock()).asObject());
        maxStockColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getMaximumStock()).asObject());
        locationColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getLocation()));
        binNumberColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getBinNumber()));
    }

    private void setupSearch() {
        productSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredData != null) {
                filteredData.setPredicate(inventory -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return inventory.getProduct().getName().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });
    }

    private void loadInventoryData() {
        try {
            filteredData = new FilteredList<>(FXCollections.observableArrayList(
                    inventoryService.getAllInventory()), p -> true);
            inventoryTable.setItems(filteredData);
        } catch (SQLException e) {
            showError("Error loading inventory data", e.getMessage());
        }
    }

    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> loadInventoryData());
        addStockButton.setOnAction(e -> handleSelectedInventory(this::showAddStockDialog));
        removeStockButton.setOnAction(e -> handleSelectedInventory(this::showRemoveStockDialog));
        stockCheckButton.setOnAction(e -> handleSelectedInventory(this::showStockCheckDialog));
        editButton.setOnAction(e -> handleSelectedInventory(this::showEditInventoryDialog));
        createButton.setOnAction(e -> showCreateInventoryDialog());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem addStockItem = new MenuItem("Add Stock");
        addStockItem.setOnAction(e -> handleSelectedInventory(this::showAddStockDialog));

        MenuItem removeStockItem = new MenuItem("Remove Stock");
        removeStockItem.setOnAction(e -> handleSelectedInventory(this::showRemoveStockDialog));

        MenuItem stockCheckItem = new MenuItem("Stock Check");
        stockCheckItem.setOnAction(e -> handleSelectedInventory(this::showStockCheckDialog));

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> handleSelectedInventory(this::showEditInventoryDialog));

        contextMenu.getItems().addAll(addStockItem, removeStockItem, stockCheckItem, editItem);
        inventoryTable.setContextMenu(contextMenu);
    }

    private void handleSelectedInventory(InventoryHandler handler) {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handler.handle(selected);
        } else {
            showError("No Selection", "Please select an inventory item.");
        }
    }

    private void showAddStockDialog(Inventory inventory) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add stock for " + inventory.getProduct().getName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity to add");
        grid.add(new Label("Quantity:"), 0, 0);
        grid.add(quantityField, 1, 0);

        TextField reasonField = new TextField();
        reasonField.setPromptText("Reason for adding stock");
        grid.add(new Label("Reason:"), 0, 1);
        grid.add(reasonField, 1, 1);

        dialogPane.setContent(grid);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || !newValue.matches("\\d+"));
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return Integer.parseInt(quantityField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(quantity -> {
            if (quantity != null) {
                try {
                    inventoryService.addStock(inventory.getId(), quantity);
                    loadInventoryData();
                } catch (Exception e) {
                    showError("Error adding stock", e.getMessage());
                }
            }
        });
    }

    private void showRemoveStockDialog(Inventory inventory) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Remove Stock");
        dialog.setHeaderText("Remove stock for " + inventory.getProduct().getName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity to remove");
        grid.add(new Label("Quantity:"), 0, 0);
        grid.add(quantityField, 1, 0);

        TextField reasonField = new TextField();
        reasonField.setPromptText("Reason for removing stock");
        grid.add(new Label("Reason:"), 0, 1);
        grid.add(reasonField, 1, 1);

        Label currentStockLabel = new Label("Current stock: " + inventory.getQuantity());
        grid.add(currentStockLabel, 0, 2, 2, 1);

        dialogPane.setContent(grid);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = false;
            if (newValue.matches("\\d+")) {
                int quantity = Integer.parseInt(newValue);
                isValid = quantity > 0 && quantity <= inventory.getQuantity();
            }
            okButton.setDisable(!isValid);
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return Integer.parseInt(quantityField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(quantity -> {
            if (quantity != null) {
                try {
                    inventoryService.removeStock(inventory.getId(), quantity);
                    loadInventoryData();
                } catch (Exception e) {
                    showError("Error removing stock", e.getMessage());
                }
            }
        });
    }

    private void showStockCheckDialog(Inventory inventory) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Stock Check");
        dialog.setHeaderText("Update actual stock count for " + inventory.getProduct().getName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField actualQuantityField = new TextField();
        actualQuantityField.setPromptText("Actual quantity counted");
        grid.add(new Label("Actual Quantity:"), 0, 0);
        grid.add(actualQuantityField, 1, 0);

        Label systemQuantityLabel = new Label("System Quantity: " + inventory.getQuantity());
        grid.add(systemQuantityLabel, 0, 1, 2, 1);

        dialogPane.setContent(grid);

        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        actualQuantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(!newValue.matches("\\d+"));
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    return Integer.parseInt(actualQuantityField.getText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(actualQuantity -> {
            if (actualQuantity != null) {
                try {
                    inventoryService.performStockCheck(inventory.getId(), actualQuantity);
                    loadInventoryData();
                } catch (Exception e) {
                    showError("Error performing stock check", e.getMessage());
                }
            }
        });
    }

    private void showEditInventoryDialog(Inventory inventory) {
        Dialog<Inventory> dialog = new Dialog<>();
        dialog.setTitle("Edit Inventory");
        dialog.setHeaderText("Edit inventory for " + inventory.getProduct().getName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField minStockField = new TextField(String.valueOf(inventory.getMinimumStock()));
        TextField maxStockField = new TextField(String.valueOf(inventory.getMaximumStock()));
        TextField locationField = new TextField(inventory.getLocation());
        TextField binNumberField = new TextField(inventory.getBinNumber());

        grid.add(new Label("Minimum Stock:"), 0, 0);
        grid.add(minStockField, 1, 0);
        grid.add(new Label("Maximum Stock:"), 0, 1);
        grid.add(maxStockField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Bin Number:"), 0, 3);
        grid.add(binNumberField, 1, 3);

        dialogPane.setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Inventory updated = new Inventory();
                    updated.setId(inventory.getId());
                    updated.setProduct(inventory.getProduct());
                    updated.setQuantity(inventory.getQuantity());
                    updated.setMinimumStock(Integer.parseInt(minStockField.getText()));
                    updated.setMaximumStock(Integer.parseInt(maxStockField.getText()));
                    updated.setLocation(locationField.getText());
                    updated.setBinNumber(binNumberField.getText());
                    return updated;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            try {
                inventoryService.updateInventory(updated.getId(), updated);
                loadInventoryData();
            } catch (Exception e) {
                showError("Error updating inventory", e.getMessage());
            }
        });
    }

    private void showCreateInventoryDialog() {
        Dialog<Inventory> dialog = new Dialog<>();
        dialog.setTitle("Create New Inventory");
        dialog.setHeaderText("Create new inventory entry");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Product selection combobox
        ComboBox<Product> productComboBox = new ComboBox<>();
        productComboBox.setItems(FXCollections.observableArrayList(productService.findAll()));
        productComboBox.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName());
                }
            }
        });
        productComboBox.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName());
                }
            }
        });

        // Employee selection combobox
        ComboBox<Employee> employeeComboBox = new ComboBox<>();
        employeeComboBox.setItems(FXCollections.observableArrayList(employeeService.findAllEmployees()));
        employeeComboBox.setCellFactory(lv -> new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                if (empty || employee == null) {
                    setText(null);
                } else {
                    setText(employee.getFullName());
                }
            }
        });
        employeeComboBox.setButtonCell(new ListCell<Employee>() {
            @Override
            protected void updateItem(Employee employee, boolean empty) {
                super.updateItem(employee, empty);
                if (empty || employee == null) {
                    setText(null);
                } else {
                    setText(employee.getFullName());
                }
            }
        });

        TextField initialQuantityField = new TextField("0");
        TextField minStockField = new TextField("0");
        TextField maxStockField = new TextField("0");
        TextField locationField = new TextField();
        TextField binNumberField = new TextField();

        grid.add(new Label("Product:"), 0, 0);
        grid.add(productComboBox, 1, 0);
        grid.add(new Label("Processed By:"), 0, 1);
        grid.add(employeeComboBox, 1, 1);
        grid.add(new Label("Initial Quantity:"), 0, 2);
        grid.add(initialQuantityField, 1, 2);
        grid.add(new Label("Minimum Stock:"), 0, 3);
        grid.add(minStockField, 1, 3);
        grid.add(new Label("Maximum Stock:"), 0, 4);
        grid.add(maxStockField, 1, 4);
        grid.add(new Label("Location:"), 0, 5);
        grid.add(locationField, 1, 5);
        grid.add(new Label("Bin Number:"), 0, 6);
        grid.add(binNumberField, 1, 6);

        dialogPane.setContent(grid);

        // Input validation
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Enable OK button only when all required fields are filled
        ChangeListener<String> textChangeListener = (observable, oldValue, newValue) -> {
            boolean isValid = productComboBox.getValue() != null &&
                    employeeComboBox.getValue() != null &&
                    initialQuantityField.getText().matches("\\d+") &&
                    minStockField.getText().matches("\\d+") &&
                    maxStockField.getText().matches("\\d+") &&
                    !locationField.getText().trim().isEmpty() &&
                    !binNumberField.getText().trim().isEmpty();
            okButton.setDisable(!isValid);
        };

        initialQuantityField.textProperty().addListener(textChangeListener);
        minStockField.textProperty().addListener(textChangeListener);
        maxStockField.textProperty().addListener(textChangeListener);
        locationField.textProperty().addListener(textChangeListener);
        binNumberField.textProperty().addListener(textChangeListener);
        productComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                textChangeListener.changed(null, null, null));
        employeeComboBox.valueProperty().addListener((obs, oldVal, newVal) ->
                textChangeListener.changed(null, null, null));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Inventory newInventory = new Inventory();
                    newInventory.setProduct(productComboBox.getValue());
                    newInventory.setQuantity(Integer.parseInt(initialQuantityField.getText()));
                    newInventory.setMinimumStock(Integer.parseInt(minStockField.getText()));
                    newInventory.setMaximumStock(Integer.parseInt(maxStockField.getText()));
                    newInventory.setLocation(locationField.getText());
                    newInventory.setBinNumber(binNumberField.getText());
                    return newInventory;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newInventory -> {
            try {
                Employee selectedEmployee = employeeComboBox.getValue();
                inventoryService.createInventory(newInventory, selectedEmployee.getId());
                loadInventoryData();
            } catch (Exception e) {
                showError("Error creating inventory", e.getMessage());
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private interface InventoryHandler {
        void handle(Inventory inventory);
    }
}
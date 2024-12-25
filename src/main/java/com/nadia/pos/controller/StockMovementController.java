package com.nadia.pos.controller;

import com.nadia.pos.enums.StockMovementType;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Product;
import com.nadia.pos.model.StockMovement;
import com.nadia.pos.service.StockMovementService;
import com.nadia.pos.service.ProductService;
import com.nadia.pos.service.EmployeeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.stage.FileChooser;
import com.nadia.pos.utils.StockMovementPdfGenerator;

public class StockMovementController implements Initializable {
    private final StockMovementService stockMovementService;
    private final ProductService productService;
    private final EmployeeService employeeService;

    @FXML private TableView<StockMovement> movementTable;
    @FXML private TableColumn<StockMovement, String> dateColumn;
    @FXML private TableColumn<StockMovement, String> typeColumn;
    @FXML private TableColumn<StockMovement, String> productColumn;
    @FXML private TableColumn<StockMovement, String> quantityColumn;
    @FXML private TableColumn<StockMovement, String> referenceColumn;
    @FXML private TableColumn<StockMovement, String> reasonColumn;
    @FXML private TableColumn<StockMovement, String> processedByColumn;

    @FXML private ComboBox<StockMovementType> movementTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField referenceSearchField;
    @FXML private Button searchButton;
    @FXML private Button newMovementButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button generatePdfButton;

    public StockMovementController(StockMovementService stockMovementService,
                                   ProductService productService,
                                   EmployeeService employeeService) {
        this.stockMovementService = stockMovementService;
        this.productService = productService;
        this.employeeService = employeeService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupMovementTypeCombo();
        setupDatePickers();
        setupEventHandlers();
        setupContextMenu();
        loadInitialData();
    }

    private void setupTableColumns() {
        TableColumn<StockMovement, Integer> snCol = new TableColumn<>("SN");
        snCol.setCellFactory(col -> new TableCell<StockMovement, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatedAt().toString()));
        typeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType().toString()));
        productColumn.setCellValueFactory(data -> {
            StockMovement movement = data.getValue();
            Product product = movement.getProduct();
            System.out.println("Product ID: " + (product != null ? product.getId() : "null"));
            System.out.println("Product Object: " + product);
            return new SimpleStringProperty(product != null ? product.getName() : "Product ID: " + movement.getProduct().getId());
        });
        quantityColumn.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        referenceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReferenceNumber()));
        reasonColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getReason()));
        processedByColumn.setCellValueFactory(data -> {
            StockMovement movement = data.getValue();
            Employee employee = movement.getProcessedBy();
            System.out.println("Employee ID: " + (employee != null ? employee.getId() : "null"));
            System.out.println("Employee Object: " + employee);
            return new SimpleStringProperty(employee != null ? employee.getFullName() : "Employee ID: " + movement.getProcessedBy().getId());
        });
    }

    private void setupMovementTypeCombo() {
        movementTypeCombo.setItems(FXCollections.observableArrayList(StockMovementType.values()));
        movementTypeCombo.setConverter(new StringConverter<StockMovementType>() {
            @Override
            public String toString(StockMovementType type) {
                return type != null ? type.toString() : "All Types";
            }

            @Override
            public StockMovementType fromString(String string) {
                return string != null ? StockMovementType.valueOf(string) : null;
            }
        });
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(e -> searchMovements());
//        newMovementButton.setOnAction(e -> showNewMovementDialog());
        viewDetailsButton.setOnAction(e -> showMovementDetails());
        generatePdfButton.setOnAction(e -> printMovement());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewItem = new MenuItem("View Details");
        viewItem.setOnAction(e -> showMovementDetails());

        MenuItem printItem = new MenuItem("Print Movement");
        printItem.setOnAction(e -> printMovement());

        contextMenu.getItems().addAll(viewItem, printItem);
        movementTable.setContextMenu(contextMenu);
    }

    private void searchMovements() {
        try {
            List<StockMovement> movements;
            if (!referenceSearchField.getText().isEmpty()) {
                movements = stockMovementService.getMovementsByReference(referenceSearchField.getText());
            } else {
                movements = stockMovementService.getAllMovements();
            }

            FilteredList<StockMovement> filteredData = new FilteredList<>(FXCollections.observableArrayList(movements));
            if (movementTypeCombo.getValue() != null) {
                filteredData.setPredicate(movement ->
                        movement.getType() == movementTypeCombo.getValue());
            }

            movementTable.setItems(filteredData);
        } catch (SQLException e) {
            showError("Error searching movements", e.getMessage());
        }
    }

    private void showNewMovementDialog() {
        Dialog<StockMovement> dialog = new Dialog<>();
        dialog.setTitle("New Stock Movement");
        dialog.setHeaderText("Create new stock movement");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Movement type combo
        ComboBox<StockMovementType> typeCombo = new ComboBox<>(
                FXCollections.observableArrayList(StockMovementType.values())
        );
        typeCombo.setPromptText("Select movement type");

        // Product combo
        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.setItems(FXCollections.observableArrayList(productService.findAllProducts()));
        productCombo.setPromptText("Select product");

        // Employee combo
        ComboBox<Employee> employeeCombo = new ComboBox<>();
        employeeCombo.setItems(FXCollections.observableArrayList(employeeService.findAllEmployees()));
        employeeCombo.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee employee) {
                return employee != null ? employee.getFullName() : "";
            }

            @Override
            public Employee fromString(String string) {
                return null;
            }
        });
        employeeCombo.setPromptText("Select employee");

        // Other fields
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");

        TextField referenceField = new TextField();
        referenceField.setPromptText("Enter reference number");

        TextField reasonField = new TextField();
        reasonField.setPromptText("Enter reason");

        TextField unitCostField = new TextField();
        unitCostField.setPromptText("Enter unit cost");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter notes");
        notesArea.setPrefRowCount(3);

        // Add fields to grid
        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Product:"), 0, 1);
        grid.add(productCombo, 1, 1);
        grid.add(new Label("Employee:"), 0, 2);
        grid.add(employeeCombo, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Reference:"), 0, 4);
        grid.add(referenceField, 1, 4);
        grid.add(new Label("Reason:"), 0, 5);
        grid.add(reasonField, 1, 5);
        grid.add(new Label("Unit Cost:"), 0, 6);
        grid.add(unitCostField, 1, 6);
        grid.add(new Label("Notes:"), 0, 7);
        grid.add(notesArea, 1, 7);

        dialogPane.setContent(grid);

        // Enable/disable OK button based on input validation
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Validation listener
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateInput(
                okButton, typeCombo, productCombo, employeeCombo, quantityField, referenceField
        ));
        productCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateInput(
                okButton, typeCombo, productCombo, employeeCombo, quantityField, referenceField
        ));
        employeeCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateInput(
                okButton, typeCombo, productCombo, employeeCombo, quantityField, referenceField
        ));
        quantityField.textProperty().addListener((obs, oldVal, newVal) -> validateInput(
                okButton, typeCombo, productCombo, employeeCombo, quantityField, referenceField
        ));
        referenceField.textProperty().addListener((obs, oldVal, newVal) -> validateInput(
                okButton, typeCombo, productCombo, employeeCombo, quantityField, referenceField
        ));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    StockMovement movement = new StockMovement();
                    movement.setType(typeCombo.getValue());
                    movement.setProduct(productCombo.getValue());
                    movement.setQuantity(Integer.parseInt(quantityField.getText()));
                    movement.setReferenceNumber(referenceField.getText());
                    movement.setReason(reasonField.getText());
                    movement.setUnitCost(new BigDecimal(unitCostField.getText().isEmpty() ? "0" : unitCostField.getText()));
                    movement.setNotes(notesArea.getText());
                    movement.setProcessedBy(employeeCombo.getValue());
                    return movement;
                } catch (Exception e) {
                    showError("Error creating movement", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(movement -> {
            try {
                stockMovementService.recordMovement(movement);
                searchMovements();
            } catch (Exception e) {
                showError("Error recording movement", e.getMessage());
            }
        });
    }

    private void validateInput(Button okButton, ComboBox<StockMovementType> typeCombo,
                               ComboBox<Product> productCombo, ComboBox<Employee> employeeCombo,
                               TextField quantityField, TextField referenceField) {
        boolean isValid = typeCombo.getValue() != null &&
                productCombo.getValue() != null &&
                employeeCombo.getValue() != null &&
                quantityField.getText().matches("\\d+") &&
                !referenceField.getText().trim().isEmpty();
        okButton.setDisable(!isValid);
    }

    private void showMovementDetails() {
        StockMovement selected = movementTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a movement to view.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Movement Details");
        dialog.setHeaderText("Details for movement " + selected.getReferenceNumber());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Add all movement details to the grid
        int row = 0;
        grid.add(new Label("Date:"), 0, row);
        grid.add(new Label(selected.getCreatedAt().toString()), 1, row++);

        grid.add(new Label("Type:"), 0, row);
        grid.add(new Label(selected.getType().toString()), 1, row++);

        grid.add(new Label("Product:"), 0, row);
        grid.add(new Label(selected.getProduct().getName()), 1, row++);

        grid.add(new Label("Quantity:"), 0, row);
        grid.add(new Label(String.valueOf(selected.getQuantity())), 1, row++);

        grid.add(new Label("Previous Stock:"), 0, row);
        grid.add(new Label(String.valueOf(selected.getPreviousStock())), 1, row++);

        grid.add(new Label("New Stock:"), 0, row);
        grid.add(new Label(String.valueOf(selected.getNewStock())), 1, row++);

        grid.add(new Label("Reference:"), 0, row);
        grid.add(new Label(selected.getReferenceNumber()), 1, row++);

        grid.add(new Label("Reason:"), 0, row);
        grid.add(new Label(selected.getReason()), 1, row++);

        grid.add(new Label("Unit Cost:"), 0, row);
        grid.add(new Label(selected.getUnitCost().toString()), 1, row++);

        grid.add(new Label("Processed By:"), 0, row);
        grid.add(new Label(selected.getProcessedBy().getFullName()), 1, row++);

        grid.add(new Label("Notes:"), 0, row);
        TextArea notesArea = new TextArea(selected.getNotes());
        notesArea.setEditable(false);
        notesArea.setPrefRowCount(3);
        grid.add(notesArea, 1, row);

        dialogPane.setContent(grid);
        dialog.showAndWait();
    }

    private void printMovement() {
        StockMovement selected = movementTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a movement to print.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("stock-movement-" + selected.getReferenceNumber() + ".pdf");

        File file = fileChooser.showSaveDialog(movementTable.getScene().getWindow());
        if (file != null) {
            try {
                StockMovementPdfGenerator pdfGenerator = new StockMovementPdfGenerator();
                pdfGenerator.generatePdf(selected, file.getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("PDF report has been generated successfully.");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error Generating PDF", "Failed to generate PDF report: " + e.getMessage());
            }
        }
    }

    private void loadInitialData() {
        searchMovements();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
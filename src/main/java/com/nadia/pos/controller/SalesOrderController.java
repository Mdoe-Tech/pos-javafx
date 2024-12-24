package com.nadia.pos.controller;

import com.nadia.pos.model.*;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.service.ProductService;
import com.nadia.pos.service.SalesOrderService;
import com.nadia.pos.enums.SalesType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesOrderController {
    @FXML private TableView<SalesOrder> salesOrderTable;
    @FXML private TableColumn<SalesOrder, String> orderNumberCol;
    @FXML private TableColumn<SalesOrder, Customer> customerCol;
    @FXML private TableColumn<SalesOrder, SalesType> typeCol;
    @FXML private TableColumn<SalesOrder, LocalDateTime> deliveryDateCol;

    @FXML private TableView<SalesOrderItem> itemsTable;
    @FXML private TableColumn<SalesOrderItem, Product> productCol;
    @FXML private TableColumn<SalesOrderItem, Integer> quantityCol;
    @FXML private TableColumn<SalesOrderItem, BigDecimal> priceCol;

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<SalesType> typeCombo;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField addressField;
    @FXML private TextArea notesField;

    private final SalesOrderService salesOrderService;
    private final CustomerService customerService;
    private final ObservableList<SalesOrder> salesOrders;
    private final ObservableList<Customer> customerList;
    private final ObservableList<SalesOrderItem> orderItems = FXCollections.observableArrayList();

    public SalesOrderController(SalesOrderService salesOrderService, CustomerService customerService) {
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
        this.salesOrders = FXCollections.observableArrayList();
        this.customerList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadSalesOrders();
        setupCustomerComboBox();
        setupComboBoxes();
    }

    private void setupColumns() {
        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        deliveryDateCol.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));

        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    private void setupComboBoxes() {
        typeCombo.setItems(FXCollections.observableArrayList(SalesType.values()));
    }

    private void loadSalesOrders() {
        salesOrders.clear();
        salesOrders.addAll(salesOrderService.findAll());
        salesOrderTable.setItems(salesOrders);
    }

    private void setupCustomerComboBox() {
        try {
            // Load customers
            List<Customer> customers = customerService.searchCustomers("");
            customerList.clear();
            customerList.addAll(customers);
            customerCombo.setItems(customerList);

            // Set up display format for customer names
            customerCombo.setCellFactory(param -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer customer, boolean empty) {
                    super.updateItem(customer, empty);
                    if (empty || customer == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s (%s)",
                                customer.getName(),
                                customer.getCode()));
                    }
                }
            });

            customerCombo.setButtonCell(new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer customer, boolean empty) {
                    super.updateItem(customer, empty);
                    if (empty || customer == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s (%s)",
                                customer.getName(),
                                customer.getCode()));
                    }
                }
            });

            customerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    addressField.setText(newVal.getAddress());
                }
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to load customers", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleNewSalesOrder() {
        clearFields();
    }

    @FXML
    private void handleSave() {
        try {
            SalesOrder salesOrder = gatherFormData();
            if (salesOrder.getId() == null) {
                salesOrderService.createSalesOrder(salesOrder);
            } else {
                salesOrderService.updateSalesOrder(salesOrder);
            }
            loadSalesOrders();
            showAlert("Success", "Sales order saved successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        SalesOrder selectedOrder = salesOrderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            return;
        }

        if (confirmDialog()) {
            try {
                salesOrderService.deleteSalesOrder(selectedOrder.getId());
                loadSalesOrders();
                showAlert("Success", "Sales order deleted successfully", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            SalesOrderItem item = new SalesOrderItem();
            if (showItemDialog(item)) {
                orderItems.add(item);
                refreshTotals();
            }
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRemoveItem() {
        SalesOrderItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            orderItems.remove(selectedItem);
            refreshTotals();
        }
    }

    private SalesOrder gatherFormData() {
        SalesOrder order = new SalesOrder();
        order.setCustomer(customerCombo.getValue());
        order.setType(typeCombo.getValue());
        order.setDeliveryDate(deliveryDatePicker.getValue().atStartOfDay());
        order.setDeliveryAddress(addressField.getText());
        order.setNotes(notesField.getText());
        order.setItems(new ArrayList<>(orderItems));
        return order;
    }

    private void clearFields() {
        customerCombo.setValue(null);
        typeCombo.setValue(null);
        deliveryDatePicker.setValue(null);
        addressField.clear();
        notesField.clear();
        orderItems.clear();
    }

    private void refreshTotals() {
        SalesOrder currentOrder = gatherFormData();
        currentOrder.calculateTotal();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean confirmDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Sales Order");
        alert.setContentText("Are you sure you want to delete this sales order?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private boolean showItemDialog(SalesOrderItem item) {
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Order Item");
        dialog.setHeaderText("Enter item details");

        // Create dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Product> productCombo = new ComboBox<>();
        TextField quantityField = new TextField();
        TextField priceField = new TextField();

        grid.add(new Label("Product:"), 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set initial values if editing existing item
        if (item.getProduct() != null) {
            productCombo.setValue(item.getProduct());
            quantityField.setText(String.valueOf(item.getQuantity()));
        }

        // Add validation
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Enable OK button only when fields are valid
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateFields(okButton, productCombo, quantityField, priceField);
        });
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateFields(okButton, productCombo, quantityField, priceField);
        });
        productCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateFields(okButton, productCombo, quantityField, priceField);
        });

        // Handle result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            item.setProduct(productCombo.getValue());
            item.setQuantity(Integer.parseInt(quantityField.getText()));
            return true;
        }
        return false;
    }

    private void validateFields(Node okButton, ComboBox<Product> productCombo,
                                TextField quantityField, TextField priceField) {
        boolean valid = productCombo.getValue() != null &&
                quantityField.getText().matches("\\d+") &&
                priceField.getText().matches("\\d+(\\.\\d{0,2})?");
        okButton.setDisable(!valid);
    }
}
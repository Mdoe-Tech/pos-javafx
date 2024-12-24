package com.nadia.pos.controller;

import com.nadia.pos.model.*;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.service.ProductService;
import com.nadia.pos.service.SalesOrderService;
import com.nadia.pos.service.EmployeeService;
import com.nadia.pos.enums.SalesType;
import com.nadia.pos.enums.OrderStatus;
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
    @FXML private TableColumn<SalesOrder, Integer> snCol;
    @FXML private TableColumn<SalesOrder, String> orderNumberCol;
    @FXML private TableColumn<SalesOrder, Customer> customerCol;
    @FXML private TableColumn<SalesOrder, SalesType> typeCol;
    @FXML private TableColumn<SalesOrder, LocalDateTime> orderDateCol;
    @FXML private TableColumn<SalesOrder, LocalDateTime> deliveryDateCol;
    @FXML private TableColumn<SalesOrder, OrderStatus> statusCol;
    @FXML private TableColumn<SalesOrder, BigDecimal> totalAmountCol;

    @FXML private TableView<SalesOrderItem> itemsTable;
    @FXML private TableColumn<SalesOrderItem, Product> productCol;
    @FXML private TableColumn<SalesOrderItem, Integer> quantityCol;
    @FXML private TableColumn<SalesOrderItem, BigDecimal> subtotalCol;

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<SalesType> typeCombo;
    @FXML private ComboBox<OrderStatus> statusCombo;
    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField addressField;
    @FXML private TextArea notesField;
    @FXML private TextField taxField;
    @FXML private TextField discountField;
    @FXML private Label totalAmountLabel;
    @FXML private Label finalTotalLabel;

    private final SalesOrderService salesOrderService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final ProductService productService;
    private final ObservableList<SalesOrder> salesOrders;
    private final ObservableList<Customer> customerList;
    private final ObservableList<Employee> employeeList;
    private final ObservableList<Product> productList;
    private final ObservableList<SalesOrderItem> orderItems = FXCollections.observableArrayList();
    private SalesOrder currentOrder;

    public SalesOrderController(SalesOrderService salesOrderService,
                                CustomerService customerService,
                                EmployeeService employeeService,
                                ProductService productService
                                ) {
        this.salesOrderService = salesOrderService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.productService = productService;
        this.salesOrders = FXCollections.observableArrayList();
        this.customerList = FXCollections.observableArrayList();
        this.employeeList = FXCollections.observableArrayList();
        this.productList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        System.out.println("Initializing SalesOrderController");

        try {
            List<Product> products = productService.findAll();
            productList.setAll(products);
        } catch (Exception e) {
            showAlert("Error", "Failed to load products: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        setupColumns();
        loadSalesOrders();
        setupComboBoxes();
        setupListeners();
        handleNewSalesOrder();
    }

    private void setupColumns() {
        // Main table columns
        snCol.setCellFactory(col -> new TableCell<SalesOrder, Integer>() {
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
        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        deliveryDateCol.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Items table columns
        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        itemsTable.setItems(orderItems);
    }

    private void setupComboBoxes() {
        typeCombo.setItems(FXCollections.observableArrayList(SalesType.values()));
        statusCombo.setItems(FXCollections.observableArrayList(OrderStatus.values()));
        setupCustomerComboBox();
        setupEmployeeComboBox();
    }

    private void setupCustomerComboBox() {
        try {
            List<Customer> customers = customerService.searchCustomers("");
            customerList.setAll(customers);
            customerCombo.setItems(customerList);
            setupComboBoxDisplay(customerCombo);
        } catch (Exception e) {
            showAlert("Error", "Failed to load customers", Alert.AlertType.ERROR);
        }
    }

    private void setupEmployeeComboBox() {
        try {
            List<Employee> employees = employeeService.findAllEmployees();
            employeeList.setAll(employees);
            employeeCombo.setItems(employeeList);
            setupComboBoxDisplay(employeeCombo);
        } catch (Exception e) {
            showAlert("Error", "Failed to load employees", Alert.AlertType.ERROR);
        }
    }

    private <T> void setupComboBoxDisplay(ComboBox<T> comboBox) {
        comboBox.setCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        comboBox.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
    }

    private void setupListeners() {
        customerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                addressField.setText(newVal.getAddress());
            }
        });

        taxField.textProperty().addListener((obs, oldVal, newVal) -> refreshTotals());
        discountField.textProperty().addListener((obs, oldVal, newVal) -> refreshTotals());
    }

    private void loadSalesOrders() {
        try {
            List<SalesOrder> orders = salesOrderService.findAll();
            salesOrders.setAll(orders);
            salesOrderTable.setItems(salesOrders);

            salesOrderTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            loadOrderDetails(newValue);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load sales orders: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadOrderDetails(SalesOrder order) {
        currentOrder = order;
        customerCombo.setValue(order.getCustomer());
        typeCombo.setValue(order.getType());
        deliveryDatePicker.setValue(order.getDeliveryDate().toLocalDate());
        addressField.setText(order.getDeliveryAddress());
        notesField.setText(order.getNotes());
        statusCombo.setValue(order.getStatus());
        employeeCombo.setValue(order.getCreatedBy());
        taxField.setText(order.getTax().toString());
        discountField.setText(order.getDiscount().toString());

        List<SalesOrderItem> items = order.getItems().stream()
                .map(item -> {
                    SalesOrderItem salesItem = (SalesOrderItem) item;
                    salesItem.setSalesOrderId(order.getId());
                    return salesItem;
                })
                .collect(java.util.stream.Collectors.toList());
        orderItems.setAll(items);

        refreshTotals();
    }

    @FXML
    private void handleNewSalesOrder() {
        currentOrder = new SalesOrder();
        currentOrder.setOrderDate(LocalDateTime.now());
        currentOrder.setStatus(OrderStatus.PENDING);
        currentOrder.setOrderNumber(generateOrderNumber());
        clearFields();
    }

    private String generateOrderNumber() {
        return "SO-" + System.currentTimeMillis();
    }

    @FXML
    private void handleSave() {
        try {
            if (currentOrder.getOrderNumber() == null) {
                currentOrder.setOrderNumber(generateOrderNumber());
            }

            updateCurrentOrder();
            currentOrder.validate();

            if (currentOrder.getId() == null) {
                salesOrderService.createSalesOrder(currentOrder);
            } else {
                salesOrderService.updateSalesOrder(currentOrder);
            }
            showAlert("Success", "Sales order saved successfully", Alert.AlertType.INFORMATION);
            handleNewSalesOrder();
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateCurrentOrder() {
        String orderNumber = currentOrder.getOrderNumber();

        currentOrder.setCustomer(customerCombo.getValue());
        currentOrder.setType(typeCombo.getValue());
        currentOrder.setDeliveryDate(deliveryDatePicker.getValue().atStartOfDay());
        currentOrder.setDeliveryAddress(addressField.getText());
        currentOrder.setNotes(notesField.getText());
        currentOrder.setStatus(statusCombo.getValue());
        currentOrder.setCreatedBy(employeeCombo.getValue());

        for (OrderItem item : orderItems) {
            if (currentOrder.getId() != null) {
                item.setSalesOrderId(currentOrder.getId());
            }
        }

        currentOrder.setItems(new ArrayList<>(orderItems));

        // Restore the order number
        currentOrder.setOrderNumber(orderNumber);

        // Parse and set tax and discount
        try {
            currentOrder.setTax(new BigDecimal(taxField.getText()));
            currentOrder.setDiscount(new BigDecimal(discountField.getText()));
        } catch (NumberFormatException e) {
            currentOrder.setTax(BigDecimal.ZERO);
            currentOrder.setDiscount(BigDecimal.ZERO);
        }

        currentOrder.calculateTotal();
    }

    private void clearFields() {
        customerCombo.setValue(null);
        typeCombo.setValue(null);
        deliveryDatePicker.setValue(null);
        addressField.clear();
        notesField.clear();
        orderItems.clear();
        taxField.setText("0");
        discountField.setText("0");
        statusCombo.setValue(OrderStatus.PENDING);
        employeeCombo.setValue(null);
        totalAmountLabel.setText("0.00");
        finalTotalLabel.setText("0.00");
    }

    private void refreshTotals() {
        BigDecimal subtotal = orderItems.stream()
                .map(SalesOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = parseBigDecimal(taxField.getText());
        BigDecimal discount = parseBigDecimal(discountField.getText());

        BigDecimal finalTotal = subtotal.add(tax).subtract(discount);

        totalAmountLabel.setText(subtotal.toString());
        finalTotalLabel.setText(finalTotal.toString());
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    @FXML
    private void handleAddItem() {
        try {
            SalesOrderItem item = new SalesOrderItem();
            if (currentOrder != null && currentOrder.getId() != null) {
                item.setSalesOrderId(currentOrder.getId());
            }
            if (showItemDialog(item)) {
                orderItems.add(item);
                refreshTotals();
            }
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Order Item");
        dialog.setHeaderText("Enter item details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Create and populate product ComboBox
        ComboBox<Product> dialogProductCombo = new ComboBox<>();
        dialogProductCombo.setItems(FXCollections.observableArrayList(productService.findAll()));
        dialogProductCombo.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName()); // Assuming Product has getName() method
                }
            }
        });
        dialogProductCombo.setButtonCell(new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName()); // Assuming Product has getName() method
                }
            }
        });

        TextField quantityField = new TextField();
        TextField priceField = new TextField();

        // Configure field sizes
        dialogProductCombo.setPrefWidth(200);
        quantityField.setPrefWidth(100);
        priceField.setPrefWidth(100);

        grid.add(new Label("Product:"), 0, 0);
        grid.add(dialogProductCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set initial values if editing
        if (item.getProduct() != null) {
            dialogProductCombo.setValue(item.getProduct());
            quantityField.setText(String.valueOf(item.getQuantity()));
            priceField.setText(item.getUnitPrice().toString());
        }

        // Add validation
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Listeners for validation and auto-fill price
        dialogProductCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                priceField.setText(newVal.getPrice().toString());
            }
            validateFields(okButton, dialogProductCombo, quantityField, priceField);
        });

        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateFields(okButton, dialogProductCombo, quantityField, priceField);
        });

        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateFields(okButton, dialogProductCombo, quantityField, priceField);
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            item.setProduct(dialogProductCombo.getValue());
            item.setQuantity(Integer.parseInt(quantityField.getText()));
            item.setUnitPrice(new BigDecimal(priceField.getText()));
            item.getSubtotal();
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
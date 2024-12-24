package com.nadia.pos.controller;

import com.nadia.pos.model.*;
import com.nadia.pos.service.EmployeeService;
import com.nadia.pos.service.OrderService;
import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderController {
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> orderNumberCol;
    @FXML private TableColumn<Order, LocalDateTime> orderDateCol;
    @FXML private TableColumn<Order, BigDecimal> totalAmountCol;
    @FXML private TableColumn<Order, OrderStatus> statusCol;

    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TableColumn<OrderItem, String> productCol;
    @FXML private TableColumn<OrderItem, Integer> quantityCol;
    @FXML private TableColumn<OrderItem, BigDecimal> priceCol;

    @FXML private TextField orderNumberField;
    @FXML private TextArea notesField;
    @FXML private ComboBox<Employee> employeeCombo;
    @FXML private ComboBox<OrderStatus> statusCombo;
    @FXML private TextField discountField;

    private final ObservableList<Employee> employeeList;
    private final OrderService<Order> orderService;
    private final EmployeeService employeeService;
    private final ObservableList<Order> orders;
    private final ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();

    public OrderController(OrderService<Order> orderService, EmployeeService employeeService) {
        this.orderService = orderService;
        this.employeeService = employeeService;
        this.orders = FXCollections.observableArrayList();
        this.employeeList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupColumns();
        setupListeners();
        setupEmployeeComboBox();
        loadOrders();
    }

    private void setupColumns() {
        orderNumberCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        productCol.setCellValueFactory(new PropertyValueFactory<>("product"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    private void setupListeners() {
        orderTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        displayOrderDetails(newSelection);
                    }
                }
        );
    }

    private void loadOrders() {
        orders.clear();
        orders.addAll(orderService.findAll());
        orderTable.setItems(orders);
    }

    private void setupEmployeeComboBox() {
        // Load employees
        try {
            List<Employee> employees = employeeService.findEmployeesByDepartment("Sales");
            employeeList.clear();
            employeeList.addAll(employees);
            employeeCombo.setItems(employeeList);

            // Set up display format for employee names
            employeeCombo.setCellFactory(param -> new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee employee, boolean empty) {
                    super.updateItem(employee, empty);
                    if (empty || employee == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s (%s)",
                                employee.getFullName(),
                                employee.getEmployeeId()));
                    }
                }
            });

            // Set up display for selected employee
            employeeCombo.setButtonCell(new ListCell<Employee>() {
                @Override
                protected void updateItem(Employee employee, boolean empty) {
                    super.updateItem(employee, empty);
                    if (empty || employee == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s (%s)",
                                employee.getFullName(),
                                employee.getEmployeeId()));
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to load employees", Alert.AlertType.ERROR);
        }
    }

    private void displayOrderDetails(Order order) {
        orderNumberField.setText(order.getOrderNumber());
        notesField.setText(order.getNotes());
        employeeCombo.setValue(order.getCreatedBy());
        statusCombo.setValue(order.getStatus());
        discountField.setText(order.getDiscount().toString());

        orderItems.clear();
        orderItems.addAll(order.getItems());
        itemsTable.setItems(orderItems);
    }

    @FXML
    private void handleSave() {
        try {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                return;
            }

            selectedOrder.setNotes(notesField.getText());
            selectedOrder.setDiscount(new BigDecimal(discountField.getText()));
            selectedOrder.setStatus(statusCombo.getValue());

            orderService.updateOrder(selectedOrder);
            loadOrders();
            showAlert("Success", "Order updated successfully", Alert.AlertType.INFORMATION);
        } catch (ValidationException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            return;
        }

        if (confirmDialog("Delete Order", "Are you sure you want to delete this order?")) {
            try {
                orderService.updateOrderStatus(selectedOrder.getOrderNumber(), OrderStatus.CANCELLED);
                loadOrders();
                showAlert("Success", "Order deleted successfully", Alert.AlertType.INFORMATION);
            } catch (ValidationException e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean confirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
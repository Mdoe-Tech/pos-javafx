package com.nadia.pos.controller;

import com.nadia.pos.model.Customer;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.enums.CustomerType;
import com.nadia.pos.exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {
    private final CustomerService customerService;

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> codeColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, CustomerType> typeColumn;

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<CustomerType> typeComboBox;
    @FXML private TextField creditLimitField;
    @FXML private TextField searchField;

    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;

    private Customer selectedCustomer;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupControls();
        loadCustomers();

        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                loadCustomers();
            } else {
                customerTable.setItems(FXCollections.observableArrayList(
                        customerService.searchCustomers(newValue)
                ));
            }
        });
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedCustomer = newSelection;
                    populateFields(newSelection);
                    deleteButton.setDisable(newSelection == null);
                }
        );
    }

    private void setupControls() {
        typeComboBox.setItems(FXCollections.observableArrayList(CustomerType.values()));
        clearFields();
    }

    @FXML
    private void handleSave() {
        try {
            Customer customer = new Customer();
            if (selectedCustomer != null) {
                customer.setId(selectedCustomer.getId());
            }

            customer.setCode(codeField.getText());
            customer.setName(nameField.getText());
            customer.setPhone(phoneField.getText());
            customer.setEmail(emailField.getText());
            customer.setType(typeComboBox.getValue());
            customer.setCreditLimit(new BigDecimal(creditLimitField.getText().isEmpty() ? "0" : creditLimitField.getText()));

            if (selectedCustomer == null) {
                customerService.createCustomer(customer);
            } else {
                customerService.updateCustomer(customer);
            }

            loadCustomers();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Customer " + (selectedCustomer == null ? "created" : "updated") + " successfully!");

        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid credit limit");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedCustomer == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Customer");
        alert.setContentText("Are you sure you want to delete this customer?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    customerService.deleteCustomer(selectedCustomer);
                    loadCustomers();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Customer deleted successfully!");
                } catch (ValidationException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void loadCustomers() {
        customerTable.setItems(FXCollections.observableArrayList(
                customerService.searchCustomers("")
        ));
    }

    private void populateFields(Customer customer) {
        if (customer == null) {
            clearFields();
            return;
        }

        codeField.setText(customer.getCode());
        nameField.setText(customer.getName());
        phoneField.setText(customer.getPhone());
        emailField.setText(customer.getEmail());
        typeComboBox.setValue(customer.getType());
        creditLimitField.setText(customer.getCreditLimit().toString());
    }

    private void clearFields() {
        selectedCustomer = null;
        codeField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        typeComboBox.setValue(CustomerType.RETAIL);
        creditLimitField.clear();
        deleteButton.setDisable(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
package com.nadia.pos.controller;

import com.nadia.pos.model.Customer;
import com.nadia.pos.service.CustomerService;
import com.nadia.pos.enums.CustomerType;
import com.nadia.pos.exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {
    @FXML private Button refreshButton;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> codeColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, CustomerType> typeColumn;
    @FXML private TableColumn<Customer, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<CustomerType> typeComboBox;
    @FXML private TextField creditLimitField;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final CustomerService customerService;
    private Customer selectedCustomer;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupControls();
        setupValidation();
        loadCustomers();
    }

    private void setupTable() {
        setupTableColumns();
        setupActionColumn();
        setupTableSelection();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    }

    private void setupActionColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actions = new HBox(5, editBtn, deleteBtn);

            {
                actions.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> handleEdit(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });
    }

    private void setupTableSelection() {
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newSelection) -> populateFields(newSelection)
        );
    }

    private void setupControls() {
        typeComboBox.setItems(FXCollections.observableArrayList(CustomerType.values()));
        searchField.textProperty().addListener((obs, old, newValue) ->
                customerTable.setItems(FXCollections.observableArrayList(
                        customerService.searchCustomers(newValue.trim())
                ))
        );
        refreshButton.setOnAction(e -> loadCustomers());
    }

    private void setupValidation() {
        saveButton.disableProperty().bind(
                nameField.textProperty().isEmpty()
                        .or(phoneField.textProperty().isEmpty())
                        .or(typeComboBox.valueProperty().isNull())
        );
    }

    private void handleEdit(Customer customer) {
        selectedCustomer = customer;
        populateFields(customer);
    }

    @FXML
    private void handleSave() {
        try {
            Customer customer = createCustomerFromFields();
            if (selectedCustomer == null) {
                customerService.createCustomer(customer);
            } else {
                customer.setId(selectedCustomer.getId());
                customerService.updateCustomer(customer);
            }

            clearFields();
            loadCustomers();
            showSuccess("Customer " + (selectedCustomer == null ? "created" : "updated"));

        } catch (Exception e) {
            showError(e);
        }
    }

    private Customer createCustomerFromFields() {
        Customer customer = new Customer();
        customer.setCode(codeField.getText());
        customer.setName(nameField.getText());
        customer.setPhone(phoneField.getText());
        customer.setEmail(emailField.getText());
        customer.setType(typeComboBox.getValue());
        customer.setCreditLimit(new BigDecimal(creditLimitField.getText().isEmpty() ? "0" : creditLimitField.getText()));
        return customer;
    }

    private void handleDelete(Customer customer) {
        if (customer == null) return;

        new Alert(Alert.AlertType.CONFIRMATION, "Delete customer " + customer.getName() + "?")
                .showAndWait()
                .filter(r -> r == ButtonType.OK)
                .ifPresent(r -> {
                    try {
                        customerService.deleteCustomer(customer);
                        loadCustomers();
                        clearFields();
                        showSuccess("Customer deleted");
                    } catch (Exception e) {
                        showError(e);
                    }
                });
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        selectedCustomer = null;
        codeField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        typeComboBox.setValue(null);
        creditLimitField.clear();
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

    private void loadCustomers() {
        customerTable.setItems(FXCollections.observableArrayList(
                customerService.searchCustomers("")
        ));
    }

    private void showSuccess(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).show();
    }

    private void showError(Exception e) {
        String message = e instanceof ValidationException ?
                e.getMessage() : "An unexpected error occurred";
        new Alert(Alert.AlertType.ERROR, message).show();
    }
}
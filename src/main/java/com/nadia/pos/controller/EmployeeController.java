package com.nadia.pos.controller;

import com.nadia.pos.model.Employee;
import com.nadia.pos.service.EmployeeService;
import com.nadia.pos.enums.EmployeeStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class EmployeeController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> snCol;
    @FXML private TableColumn<Employee, String> employeeIdCol;
    @FXML private TableColumn<Employee, String> firstNameCol;
    @FXML private TableColumn<Employee, String> lastNameCol;
    @FXML private TableColumn<Employee, String> departmentCol;
    @FXML private TableColumn<Employee, String> positionCol;
    @FXML private TableColumn<Employee, EmployeeStatus> statusCol;

    @FXML private TextField employeeIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField departmentField;
    @FXML private TextField positionField;
    @FXML private ComboBox<EmployeeStatus> statusCombo;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Employee> supervisorCombo;

    private final EmployeeService employeeService;
    private final ObservableList<Employee> employees = FXCollections.observableArrayList();
    private Employee currentEmployee;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {
        setupColumns();
        setupComboBoxes();
        loadEmployees();
        setupListeners();
        handleNew();
    }

    private void setupColumns() {
        // Set up SN column with row numbers
        snCol.setCellFactory(col -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        employeeIdCol.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        departmentCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupComboBoxes() {
        statusCombo.setItems(FXCollections.observableArrayList(EmployeeStatus.values()));
        refreshSupervisorComboBox();
    }

    private void refreshSupervisorComboBox() {
        List<Employee> supervisors = employeeService.findAllEmployees();
        supervisorCombo.setItems(FXCollections.observableArrayList(supervisors));
        setupComboBoxDisplay(supervisorCombo);
    }

    private void setupListeners() {
        employeeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadEmployeeDetails(newValue);
                    }
                });
    }

    private void loadEmployees() {
        try {
            List<Employee> employeeList = employeeService.findAllEmployees();
            employees.setAll(employeeList);
            employeeTable.setItems(employees);
        } catch (Exception e) {
            showAlert("Error", "Failed to load employees: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadEmployeeDetails(Employee employee) {
        currentEmployee = employee;
        employeeIdField.setText(employee.getEmployeeId());
        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        emailField.setText(employee.getEmail());
        phoneField.setText(employee.getPhone());
        addressField.setText(employee.getAddress());
        dateOfBirthPicker.setValue(employee.getDateOfBirth());
        hireDatePicker.setValue(employee.getHireDate());
        departmentField.setText(employee.getDepartment());
        positionField.setText(employee.getPosition());
        statusCombo.setValue(employee.getStatus());
        usernameField.setText(employee.getUsername());
        supervisorCombo.setValue(employee.getSupervisor());
    }

    @FXML
    private void handleNew() {
        currentEmployee = new Employee();
        clearFields();
    }

    @FXML
    private void handleSave() {
        try {
            updateCurrentEmployee();

            if (currentEmployee.getId() == null) {
                employeeService.createEmployee(currentEmployee);
            } else {
                employeeService.updateEmployee(currentEmployee);
            }

            showAlert("Success", "Employee saved successfully", Alert.AlertType.INFORMATION);
            loadEmployees();
            handleNew();
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateCurrentEmployee() {
        currentEmployee.setEmployeeId(employeeIdField.getText());
        currentEmployee.setFirstName(firstNameField.getText());
        currentEmployee.setLastName(lastNameField.getText());
        currentEmployee.setEmail(emailField.getText());
        currentEmployee.setPhone(phoneField.getText());
        currentEmployee.setAddress(addressField.getText());
        currentEmployee.setDateOfBirth(dateOfBirthPicker.getValue());
        currentEmployee.setHireDate(hireDatePicker.getValue());
        currentEmployee.setDepartment(departmentField.getText());
        currentEmployee.setPosition(positionField.getText());
        currentEmployee.setStatus(statusCombo.getValue());
        currentEmployee.setUsername(usernameField.getText());
        if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
            currentEmployee.setPassword(passwordField.getText());
        }
        currentEmployee.setSupervisor(supervisorCombo.getValue());
    }

    private void clearFields() {
        employeeIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        dateOfBirthPicker.setValue(null);
        hireDatePicker.setValue(LocalDate.now());
        departmentField.clear();
        positionField.clear();
        statusCombo.setValue(EmployeeStatus.ACTIVE);
        usernameField.clear();
        passwordField.clear();
        supervisorCombo.setValue(null);
    }

    private <T> void setupComboBoxDisplay(ComboBox<T> comboBox) {
        comboBox.setCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof Employee) {
                    Employee emp = (Employee) item;
                    setText(emp.getFirstName() + " " + emp.getLastName());
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
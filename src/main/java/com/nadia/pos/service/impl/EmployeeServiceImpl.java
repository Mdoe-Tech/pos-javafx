package com.nadia.pos.service.impl;

import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.exceptions.AuthenticationException;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Role;
import com.nadia.pos.service.EmployeeService;
import com.nadia.pos.enums.EmployeeStatus;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.utils.PasswordUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeDAO employeeDAO;

    public EmployeeServiceImpl(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    @Override
    public Employee createEmployee(Employee employee) throws ValidationException {
        // Validate employee data
        employee.validate();

        // Check for duplicate employeeId, username, or email
        validateUniqueness(employee);

        // Hash the password before storing
        String hashedPassword = PasswordUtils.hashPassword(employee.getPassword());
        employee.setPassword(hashedPassword);

        // Set default values if not provided
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        return employeeDAO.save(employee);
    }

    @Override
    public Employee updateEmployee(Employee employee) throws ValidationException {
        // Validate employee exists
        employeeDAO.findById(employee.getId())
                .orElseThrow(() -> new ValidationException("Employee not found"));

        // Validate employee data
        employee.validate();

        // Check for duplicate employeeId, username, or email (excluding current employee)
        validateUniquenessForUpdate(employee);

        // Handle password update if provided
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            String hashedPassword = PasswordUtils.hashPassword(employee.getPassword());
            employee.setPassword(hashedPassword);
        }

        // Update timestamp
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeDAO.update(employee);
    }

    @Override
    public void terminateEmployee(Long id) throws ValidationException {
        Employee employee = employeeDAO.findById(id)
                .orElseThrow(() -> new ValidationException("Employee not found"));

        employee.setStatus(EmployeeStatus.TERMINATED);
        employee.setUpdatedAt(LocalDateTime.now());

        employeeDAO.update(employee);
    }

    @Override
    public Employee authenticate(String username, String password) throws AuthenticationException {
        Employee employee = employeeDAO.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }

        if (!PasswordUtils.verifyPassword(password, employee.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        return employee;
    }

    @Override
    public void updateRoles(Long employeeId, Set<Role> roles) throws ValidationException {
        Employee employee = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new ValidationException("Employee not found"));

        if (roles == null || roles.isEmpty()) {
            throw new ValidationException("Employee must have at least one role");
        }

        employee.setRoles(roles);
        employee.setUpdatedAt(LocalDateTime.now());

        employeeDAO.update(employee);
    }

    @Override
    public void updateSupervisor(Long employeeId, Long supervisorId) throws ValidationException {
        Employee employee = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new ValidationException("Employee not found"));

        Employee supervisor = supervisorId != null ?
                employeeDAO.findById(supervisorId)
                        .orElseThrow(() -> new ValidationException("Supervisor not found")) :
                null;

        employee.setSupervisor(supervisor);
        employee.setUpdatedAt(LocalDateTime.now());

        employeeDAO.update(employee);
    }

    @Override
    public Optional<Employee> findEmployeeById(Long id) {
        return employeeDAO.findById(id);
    }

    @Override
    public Optional<Employee> findEmployeeByEmployeeId(String employeeId) {
        return employeeDAO.findByEmployeeId(employeeId);
    }

    @Override
    public List<Employee> findEmployeesByDepartment(String department) {
        return employeeDAO.findByDepartment(department);
    }

    @Override
    public List<Employee> findAllEmployees() {
        return employeeDAO.findAll();
    }

    private void validateUniqueness(Employee employee) throws ValidationException {
        // Check unique employeeId
        if (employeeDAO.findByEmployeeId(employee.getEmployeeId()).isPresent()) {
            throw new ValidationException("Employee ID already exists");
        }

        // Check unique username
        if (employeeDAO.findByUsername(employee.getUsername()).isPresent()) {
            throw new ValidationException("Username already exists");
        }

        // Check unique email
        if (employeeDAO.findByEmail(employee.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists");
        }
    }

    private void validateUniquenessForUpdate(Employee employee) throws ValidationException {
        // Check unique employeeId
        employeeDAO.findByEmployeeId(employee.getEmployeeId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(employee.getId())) {
                        try {
                            throw new ValidationException("Employee ID already exists");
                        } catch (ValidationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        // Check unique username
        employeeDAO.findByUsername(employee.getUsername())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(employee.getId())) {
                        try {
                            throw new ValidationException("Username already exists");
                        } catch (ValidationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        // Check unique email
        employeeDAO.findByEmail(employee.getEmail())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(employee.getId())) {
                        try {
                            throw new ValidationException("Email already exists");
                        } catch (ValidationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
}

package com.nadia.pos.service;

import com.nadia.pos.exceptions.AuthenticationException;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Role;
import com.nadia.pos.exceptions.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EmployeeService {
    Employee createEmployee(Employee employee) throws ValidationException;

    Employee updateEmployee(Employee employee) throws ValidationException;

    void terminateEmployee(Long id) throws ValidationException;

    Employee authenticate(String username, String password) throws AuthenticationException;

    void updateRoles(Long employeeId, Set<Role> roles) throws ValidationException;

    void updateSupervisor(Long employeeId, Long supervisorId) throws ValidationException;

    Optional<Employee> findEmployeeById(Long id);

    Optional<Employee> findEmployeeByEmployeeId(String employeeId);

    List<Employee> findEmployeesByDepartment(String department);

    List<Employee> findAllEmployees();
}
package com.nadia.pos.dao;

import com.nadia.pos.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeDAO extends BaseDAO<Employee> {
    Optional<Employee> findByEmployeeId(String employeeId);
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartment(String department);
}
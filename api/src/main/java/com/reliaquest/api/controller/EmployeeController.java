package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("Getting all employees");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.info("Searching employees with name containing: {}", searchString);
        return ResponseEntity.ok(employeeService.getEmployeesByNameSearch(searchString));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        log.info("Getting employee with id: {}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Getting highest salary among all employees");
        return ResponseEntity.ok(employeeService.getHighestSalaryOfEmployees());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Getting top 10 highest earning employee names");
        return ResponseEntity.ok(employeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Override
    public ResponseEntity<CreateEmployee> createEmployee(@RequestBody CreateEmployee employeeInput) {
        log.info("Creating new employee: {}", employeeInput);
        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeInput);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Deleting employee with id: {}", id);
        Employee deletedEmployee = employeeService.deleteEmployeeById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Employee: " + deletedEmployee.getEmployeeName() + " deleted successfully");
    }
}
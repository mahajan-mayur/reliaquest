package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.DeleteEmployee;
import com.reliaquest.api.model.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeClient employeeClient;
    private final RetryTemplate retryTemplate;

    public List<Employee> getAllEmployees() {
        try {
            //RetryTemplate to handle 429 responses
            return retryTemplate.execute(context -> {
                log.info("Fetching all employees. Attempt: {}", context.getRetryCount() + 1);
                ApiResponse<List<Employee>> response = employeeClient.getAllEmployees();

                if (response == null || response.getData() == null) {
                    throw new EmployeeServiceException("Failed to fetch employees: No data returned");
                }

                return response.getData();
            }, context -> {
                log.warn("Max retry attempts reached while fetching employees");
                throw new EmployeeServiceException("Failed to fetch employees after multiple attempts");
            });
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded when fetching employees", e);
                throw new EmployeeServiceException("Rate limit exceeded. Please try again later.", e);
            }
            throw new EmployeeServiceException("Error fetching employees: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error fetching all employees", e);
            throw new EmployeeServiceException("Failed to fetch employees: " + e.getMessage(), e);
        }
    }

    public List<Employee> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees by name containing: {}", searchString);

        try {
            List<Employee> allEmployees = getAllEmployees();
            List<Employee> filteredEmployees = allEmployees.stream()
                    .filter(emp -> emp.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());

            if (filteredEmployees.isEmpty()) {
                throw new EmployeeNotFoundException("No employees found with name containing: " + searchString);
            }

            return filteredEmployees;
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching employees by name: {}", searchString, e);
            throw new EmployeeServiceException("Error searching employees by name: " + e.getMessage(), e);
        }
    }

    public Employee getEmployeeById(String id) {
        log.info("Fetching employee by id: {}", id);

        try {
            return retryTemplate.execute(context -> {
                log.info("Fetching employee by id: {}. Attempt: {}", id, context.getRetryCount() + 1);
                return getAllEmployees().stream()
                        .filter(emp -> emp.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new EmployeeNotFoundException("No employee found with id: " + id));
            });
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching employee by id: {}", id, e);
            throw new EmployeeServiceException("Error fetching employee by id: " + e.getMessage(), e);
        }
    }

    public Integer getHighestSalaryOfEmployees() {
        log.info("Fetching highest salary among all employees");

        try {
            return retryTemplate.execute(context -> {
                log.info("Fetching highest salary. Attempt: {}", context.getRetryCount() + 1);
                return getAllEmployees().stream()
                        .map(Employee::getEmployeeSalary)
                        .max(Integer::compareTo)
                        .orElseThrow(() -> new EmployeeNotFoundException("No employees found to determine highest salary"));
            });
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching highest salary", e);
            throw new EmployeeServiceException("Error fetching highest salary: " + e.getMessage(), e);
        }
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top ten highest earning employee names");

        try {
            return retryTemplate.execute(context -> {
                log.info("Fetching top earners. Attempt: {}", context.getRetryCount() + 1);
                return getAllEmployees().stream()
                        .sorted(Comparator.comparingInt(Employee::getEmployeeSalary).reversed())
                        .limit(10)
                        .map(Employee::getEmployeeName)
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            log.error("Error fetching top ten highest earning employee names", e);
            throw new EmployeeServiceException("Error fetching top earners: " + e.getMessage(), e);
        }
    }

    public Employee createEmployee(CreateEmployee request) {
        log.info("Creating employee: {}", request);

        try {
            return retryTemplate.execute(context -> {
                log.info("Creating employee. Attempt: {}", context.getRetryCount() + 1);
                ApiResponse<Employee> response = employeeClient.createEmployee(request);

                if (response == null || response.getData() == null) {
                    throw new EmployeeServiceException("Failed to create employee: No data returned");
                }

                log.info("Employee created successfully: {}", response.getData());
                return response.getData();
            });
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded when creating employee", e);
                throw new EmployeeServiceException("Rate limit exceeded. Please try again later.", e);
            }
            throw new EmployeeServiceException("Error creating employee: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error creating employee: {}", request, e);
            throw new EmployeeServiceException("Failed to create employee: " + e.getMessage(), e);
        }
    }

    public Employee deleteEmployeeById(String id) {
        log.info("Deleting employee with id: {}", id);

        try {
            return retryTemplate.execute(context -> {
                log.info("Deleting employee with id: {}. Attempt: {}", id, context.getRetryCount() + 1);

                Employee employee = getEmployeeById(id);
                DeleteEmployee deleteEmployee = new DeleteEmployee(employee.getEmployeeName());

                ResponseEntity<Void> response = employeeClient.deleteEmployeeById(deleteEmployee);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Employee deleted successfully: {}", employee);
                    return employee;
                } else {
                    throw new EmployeeServiceException("Failed to delete employee: Received non-success status code "
                            + response.getStatusCode());
                }
            });
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.error("Rate limit exceeded when deleting employee", e);
                throw new EmployeeServiceException("Rate limit exceeded. Please try again later.", e);
            }
            throw new EmployeeServiceException("Error deleting employee: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error deleting employee with id: {}", id, e);
            throw new EmployeeServiceException("Error deleting employee: " + e.getMessage(), e);
        }
    }
}
package com.reliaquest.api.client;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployee;
import com.reliaquest.api.model.DeleteEmployee;
import com.reliaquest.api.model.Employee;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "employeeClient", url = "http://localhost:8112/api/v1/employee")
public interface EmployeeClient {

    @GetMapping
    ApiResponse<List<Employee>> getAllEmployees();

    @PostMapping
    ApiResponse<Employee>  createEmployee(@RequestBody CreateEmployee request);

    @DeleteMapping
    ResponseEntity<Void> deleteEmployeeById(@RequestBody DeleteEmployee request);
}
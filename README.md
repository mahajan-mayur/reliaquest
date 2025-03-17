# Employee API Assessment

## Overview

This project implements a RESTful API for employee management that interacts with a mock Employee API. The implementation follows best practices for clean coding, test-driven development, proper logging, and scalable design.

## Key Features

- **Robust Error Handling**: Custom exceptions and global exception handling for consistent error responses
- **Rate Limiting Solution**: Implementation of retry mechanism with exponential backoff to handle 429 (Too Many Requests) errors
- **Comprehensive Logging**: Detailed logs for all operations and error scenarios
- **Complete Test Coverage**: Unit and integration tests for all components
- **Clean Code Architecture**: Clear separation of concerns between controller, service, and client layers

## Tech Stack

- Java 17
- Spring Boot
- Spring Cloud OpenFeign
- Spring Retry
- JUnit 5
- Mockito
- Lombok
- Gradle

## API Endpoints

| Method | Endpoint | Request Body | Response | Description |
|--------|----------|-------------|----------|-------------|
| GET | `/api/v1/employee` | None | List of employees | Get all employees |
| GET | `/api/v1/employee/search/{searchString}` | None | List of employees | Search employees by name |
| GET | `/api/v1/employee/{id}` | None | Employee | Get employee by ID |
| GET | `/api/v1/employee/highestSalary` | None | Integer | Get highest salary |
| GET | `/api/v1/employee/topTenHighestEarningEmployeeNames` | None | List of employee names | Get top 10 highest earning employees |
| POST | `/api/v1/employee` | `{"name": "John Doe", "salary": 85000, "age": 28, "title": "Developer"}` | Employee | Create new employee |
| DELETE | `/api/v1/employee/{id}` | `{id}` | Success message with Employee Name | Delete employee by ID |


## Setup Instructions

1. Clone the repository
2. Ensure the mock server is running on `http://localhost:8112`
3. Build the project:
   ```
   ./gradlew clean build
   ```
4. Run the application:
   ```
   ./gradlew bootRun
   ```


## Design Decisions

### Retry Strategy for Rate Limiting

The mock server implements random request limits (429 Too Many Requests responses), which simulates real-world API constraints. My solution uses Spring Retry with exponential backoff to handle this elegantly:

- Initial backoff of 2 seconds
- Maximum backoff of 30 seconds
- Multiplier of 2.0 (doubles wait time between retries)
- Maximum of 3 retry attempts

This approach ensures the application can recover automatically from transient rate-limiting issues without manual intervention.

### Exception Handling Strategy

Custom exceptions are used for different error scenarios, with a central exception handler to translate these into consistent API responses:

- `EmployeeNotFoundException`: When an employee doesn't exist
- `EmployeeServiceException`: For general service-level errors

### Feign Client Implementation

OpenFeign is used for HTTP client implementation, making the API calls more concise and testable.


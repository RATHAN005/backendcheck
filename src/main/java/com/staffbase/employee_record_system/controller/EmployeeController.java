package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.EmployeeRequest;
import com.staffbase.employee_record_system.dto.EmployeeResponse;
import com.staffbase.employee_record_system.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

        private final EmployeeService employeeService;

        @PostMapping
        public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<EmployeeResponse>> createEmployee(
                        @jakarta.validation.Valid @RequestBody EmployeeRequest request) {
                EmployeeResponse response = employeeService.createEmployee(request);
                return ResponseEntity
                                .ok(com.staffbase.employee_record_system.dto.APIResponse.<EmployeeResponse>builder()
                                                .success(true)
                                                .message("Employee created successfully")
                                                .data(response)
                                                .status(200)
                                                .build());
        }

        @GetMapping
        public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<com.staffbase.employee_record_system.dto.PageResponse<EmployeeResponse>>> getEmployeesPaginated(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String department) {

                com.staffbase.employee_record_system.dto.PageResponse<EmployeeResponse> employees = employeeService
                                .getEmployees(page, size, search, department);

                return ResponseEntity.ok(
                                com.staffbase.employee_record_system.dto.APIResponse.<com.staffbase.employee_record_system.dto.PageResponse<EmployeeResponse>>builder()
                                                .success(true)
                                                .message("Employees retrieved successfully")
                                                .data(employees)
                                                .status(200)
                                                .build());
        }

        @GetMapping("/all")
        public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<List<EmployeeResponse>>> getAllEmployees() {
                List<EmployeeResponse> employees = employeeService.getAllEmployees();
                return ResponseEntity.ok(
                                com.staffbase.employee_record_system.dto.APIResponse.<List<EmployeeResponse>>builder()
                                                .success(true)
                                                .message("All employees retrieved successfully")
                                                .data(employees)
                                                .status(200)
                                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<EmployeeResponse>> getEmployeeById(
                        @PathVariable java.util.UUID id) {
                EmployeeResponse response = employeeService.getEmployeeById(id);
                return ResponseEntity
                                .ok(com.staffbase.employee_record_system.dto.APIResponse.<EmployeeResponse>builder()
                                                .success(true)
                                                .message("Employee retrieved successfully")
                                                .data(response)
                                                .status(200)
                                                .build());
        }

        @PutMapping("/{id}")
        public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<EmployeeResponse>> updateEmployee(
                        @PathVariable java.util.UUID id,
                        @jakarta.validation.Valid @RequestBody EmployeeRequest request) {
                EmployeeResponse response = employeeService.updateEmployee(id, request);
                return ResponseEntity
                                .ok(com.staffbase.employee_record_system.dto.APIResponse.<EmployeeResponse>builder()
                                                .success(true)
                                                .message("Employee updated successfully")
                                                .data(response)
                                                .status(200)
                                                .build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<Void>> deleteEmployee(
                        @PathVariable java.util.UUID id) {
                employeeService.deleteEmployee(id);
                return ResponseEntity.ok(com.staffbase.employee_record_system.dto.APIResponse.<Void>builder()
                                .success(true)
                                .message("Employee deleted successfully")
                                .status(200)
                                .build());
        }
}




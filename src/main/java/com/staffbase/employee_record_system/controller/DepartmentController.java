package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.DepartmentDTO;
import com.staffbase.employee_record_system.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<APIResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(APIResponse.<List<DepartmentDTO>>builder()
                .success(true)
                .message("Departments retrieved successfully")
                .data(departments)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<DepartmentDTO>> getDepartmentById(@PathVariable UUID id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(APIResponse.<DepartmentDTO>builder()
                .success(true)
                .message("Department found")
                .data(department)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<DepartmentDTO>> createDepartment(@RequestBody DepartmentDTO departmentDTO) {
        
        org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().forEach(System.out::println);

        DepartmentDTO created = departmentService.createDepartment(departmentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<DepartmentDTO>builder()
                        .success(true)
                        .message("Department created successfully")
                        .data(created)
                        .status(HttpStatus.CREATED.value())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<DepartmentDTO>> updateDepartment(
            @PathVariable UUID id,
            @RequestBody DepartmentDTO departmentDTO) {
        DepartmentDTO updated = departmentService.updateDepartment(id, departmentDTO);
        return ResponseEntity.ok(APIResponse.<DepartmentDTO>builder()
                .success(true)
                .message("Department updated successfully")
                .data(updated)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .success(true)
                .message("Department deleted successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build());
    }
}




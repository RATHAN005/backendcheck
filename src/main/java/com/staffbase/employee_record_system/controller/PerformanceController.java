package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.PerformanceRequest;
import com.staffbase.employee_record_system.dto.PerformanceResponse;
import com.staffbase.employee_record_system.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final PerformanceService performanceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<APIResponse<PerformanceResponse>> addReview(
            @RequestBody PerformanceRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<PerformanceResponse>builder()
                .success(true)
                .message("Performance review added")
                .data(performanceService.addReview(request, authentication.getName()))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<APIResponse<List<PerformanceResponse>>> getEmployeeReviews(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(APIResponse.<List<PerformanceResponse>>builder()
                .success(true)
                .message("Reviews retrieved successfully")
                .data(performanceService.getEmployeeReviews(employeeId))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }
}




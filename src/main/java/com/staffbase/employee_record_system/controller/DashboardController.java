package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.DashboardStatsDTO;
import com.staffbase.employee_record_system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<APIResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return ResponseEntity.ok(APIResponse.<DashboardStatsDTO>builder()
                .success(true)
                .message("Dashboard stats retrieved successfully")
                .data(stats)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }
}




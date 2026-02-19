package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.AuditLogResponse;
import com.staffbase.employee_record_system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<APIResponse<List<AuditLogResponse>>> getAllLogs() {
        return ResponseEntity.ok(APIResponse.<List<AuditLogResponse>>builder()
                .success(true)
                .message("Audit logs retrieved successfully")
                .data(auditLogService.getAllLogs())
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }
}




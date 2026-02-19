package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.LeaveApplicationRequest;
import com.staffbase.employee_record_system.dto.LeaveResponse;
import com.staffbase.employee_record_system.entity.LeaveStatus;
import com.staffbase.employee_record_system.entity.LeaveBalance;
import com.staffbase.employee_record_system.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {
        private final LeaveService leaveService;

        @PostMapping("/apply")
        public ResponseEntity<APIResponse<LeaveResponse>> applyForLeave(
                        @RequestBody LeaveApplicationRequest request,
                        Authentication authentication) {
                return ResponseEntity.ok(APIResponse.<LeaveResponse>builder()
                                .success(true)
                                .message("Leave application submitted")
                                .data(leaveService.applyForLeave(request, authentication.getName()))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @PostMapping("/{id}/action")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
        public ResponseEntity<APIResponse<LeaveResponse>> takeAction(
                        @PathVariable UUID id,
                        @RequestBody Map<String, String> body,
                        Authentication authentication) {

                LeaveStatus status = LeaveStatus.valueOf(body.get("status").toUpperCase());
                String remarks = body.get("remarks");

                return ResponseEntity.ok(APIResponse.<LeaveResponse>builder()
                                .success(true)
                                .message("Leave " + status.name().toLowerCase())
                                .data(leaveService.approveOrRejectLeave(id, status, remarks, authentication.getName()))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/balances/{employeeId}")
        public ResponseEntity<APIResponse<List<LeaveBalance>>> getBalances(
                        @PathVariable UUID employeeId) {
                List<LeaveBalance> balances = leaveService.getLeaveBalances(employeeId);
                return ResponseEntity.ok(APIResponse.<List<LeaveBalance>>builder()
                                .success(true)
                                .message("Leave balances retrieved")
                                .data(balances)
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/employee/{employeeId}")
        public ResponseEntity<APIResponse<List<LeaveResponse>>> getEmployeeLeaves(@PathVariable UUID employeeId) {
                return ResponseEntity.ok(APIResponse.<List<LeaveResponse>>builder()
                                .success(true)
                                .message("Leaves retrieved successfully")
                                .data(leaveService.getEmployeeLeaves(employeeId))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
        public ResponseEntity<APIResponse<List<LeaveResponse>>> getAllLeaves() {
                return ResponseEntity.ok(APIResponse.<List<LeaveResponse>>builder()
                                .success(true)
                                .message("All leaves retrieved successfully")
                                .data(leaveService.getAllLeaves())
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }
}

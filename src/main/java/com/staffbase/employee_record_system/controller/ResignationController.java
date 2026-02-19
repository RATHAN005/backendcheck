package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.ResignationRequest;
import com.staffbase.employee_record_system.dto.ResignationResponse;
import com.staffbase.employee_record_system.entity.ResignationStatus;
import com.staffbase.employee_record_system.service.ResignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resignations")
@RequiredArgsConstructor
public class ResignationController {

        private final ResignationService resignationService;

        @PostMapping
        public ResponseEntity<APIResponse<ResignationResponse>> applyResignation(
                        @RequestBody ResignationRequest request,
                        Authentication authentication) {

                ResignationResponse resignation = resignationService.applyResignation(authentication.getName(),
                                request);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                APIResponse.<ResignationResponse>builder()
                                                .success(true)
                                                .message("Resignation application submitted successfully")
                                                .data(resignation)
                                                .status(HttpStatus.CREATED.value())
                                                .timestamp(LocalDateTime.now())
                                                .build());
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
        public ResponseEntity<APIResponse<List<ResignationResponse>>> getAllResignations() {
                return ResponseEntity.ok(
                                APIResponse.<List<ResignationResponse>>builder()
                                                .success(true)
                                                .message("All resignations retrieved")
                                                .data(resignationService.getAllResignations())
                                                .status(HttpStatus.OK.value())
                                                .timestamp(LocalDateTime.now())
                                                .build());
        }

        @GetMapping("/my")
        public ResponseEntity<APIResponse<List<ResignationResponse>>> getMyResignations(Authentication authentication) {
                return ResponseEntity.ok(
                                APIResponse.<List<ResignationResponse>>builder()
                                                .success(true)
                                                .message("My resignations retrieved")
                                                .data(resignationService.getMyResignations(authentication.getName()))
                                                .status(HttpStatus.OK.value())
                                                .timestamp(LocalDateTime.now())
                                                .build());
        }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
        public ResponseEntity<APIResponse<ResignationResponse>> updateStatus(
                        @PathVariable UUID id,
                        @RequestParam ResignationStatus status,
                        @RequestParam(required = false) String remarks) {

                ResignationResponse resignation = resignationService.updateResignationStatus(id, status, remarks);
                return ResponseEntity.ok(
                                APIResponse.<ResignationResponse>builder()
                                                .success(true)
                                                .message("Resignation status updated to " + status)
                                                .data(resignation)
                                                .status(HttpStatus.OK.value())
                                                .timestamp(LocalDateTime.now())
                                                .build());
        }
}

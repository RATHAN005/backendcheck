package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.entity.Designation;
import com.staffbase.employee_record_system.entity.Location;
import com.staffbase.employee_record_system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class AdminController {

    private final AdminService adminService;

    private <T> APIResponse<T> createResponse(T data, String message) {
        return APIResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Designations
    @GetMapping("/designations")
    public ResponseEntity<APIResponse<List<Designation>>> getAllDesignations() {
        return ResponseEntity.ok(createResponse(adminService.getAllDesignations(), "Designations retrieved"));
    }

    @PostMapping("/designations")
    public ResponseEntity<APIResponse<Designation>> addDesignation(@RequestBody Designation designation,
            Principal principal) {
        return ResponseEntity
                .ok(createResponse(adminService.addDesignation(designation, principal.getName()), "Designation added"));
    }

    @DeleteMapping("/designations/{id}")
    public ResponseEntity<APIResponse<Void>> deleteDesignation(@PathVariable UUID id, Principal principal) {
        adminService.deleteDesignation(id, principal.getName());
        return ResponseEntity.ok(createResponse(null, "Designation deleted"));
    }

    // Locations
    @GetMapping("/locations")
    public ResponseEntity<APIResponse<List<Location>>> getAllLocations() {
        return ResponseEntity.ok(createResponse(adminService.getAllLocations(), "Locations retrieved"));
    }

    @PostMapping("/locations")
    public ResponseEntity<APIResponse<Location>> addLocation(@RequestBody Location location, Principal principal) {
        return ResponseEntity
                .ok(createResponse(adminService.addLocation(location, principal.getName()), "Location added"));
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<APIResponse<Void>> deleteLocation(@PathVariable UUID id, Principal principal) {
        adminService.deleteLocation(id, principal.getName());
        return ResponseEntity.ok(createResponse(null, "Location deleted"));
    }

    // System Settings
    @GetMapping("/settings")
    public ResponseEntity<APIResponse<Map<String, String>>> getSystemSettings() {
        return ResponseEntity.ok(createResponse(adminService.getSystemSettings(), "Settings retrieved"));
    }

    @PostMapping("/settings")
    public ResponseEntity<APIResponse<Void>> updateSystemSettings(@RequestBody Map<String, String> settings,
            Principal principal) {
        adminService.updateSystemSettings(settings, principal.getName());
        return ResponseEntity.ok(createResponse(null, "Settings updated"));
    }
}

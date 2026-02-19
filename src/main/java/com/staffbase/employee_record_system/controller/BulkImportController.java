package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.service.BulkImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkImportService bulkImportService;

    @PostMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Map<String, Object>> importEmployees(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        return ResponseEntity.ok(bulkImportService.importEmployees(file, principal.getName()));
    }
}




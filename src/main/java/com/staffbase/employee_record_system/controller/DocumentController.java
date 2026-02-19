package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.DocumentUploadDTO;
import com.staffbase.employee_record_system.entity.EmployeeDocument;
import com.staffbase.employee_record_system.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

        private final DocumentService documentService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<APIResponse<EmployeeDocument>> uploadDocument(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "description", required = false) String description,
                        Authentication authentication) {

                DocumentUploadDTO uploadDTO = DocumentUploadDTO.builder()
                                .file(file)
                                .description(description)
                                .build();

                EmployeeDocument document = documentService.uploadDocument(uploadDTO, authentication.getName());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(APIResponse.<EmployeeDocument>builder()
                                                .success(true)
                                                .message("File uploaded successfully")
                                                .data(document)
                                                .status(HttpStatus.CREATED.value())
                                                .timestamp(LocalDateTime.now())
                                                .build());
        }

        @GetMapping
        public ResponseEntity<APIResponse<List<EmployeeDocument>>> getAllDocuments(Authentication authentication) {
                String username = authentication.getName();
                boolean isAdminOrManagerOrHR = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                a.getAuthority().equals("ROLE_MANAGER") ||
                                                a.getAuthority().equals("ROLE_HR"));

                List<EmployeeDocument> documents;
                if (isAdminOrManagerOrHR) {
                        documents = documentService.getAllDocuments();
                } else {
                        documents = documentService.getDocumentsForUser(username);
                }

                return ResponseEntity.ok(APIResponse.<List<EmployeeDocument>>builder()
                                .success(true)
                                .message("Documents retrieved successfully")
                                .data(documents)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/{id}")
        public ResponseEntity<APIResponse<EmployeeDocument>> getDocumentById(@PathVariable String id) {
                EmployeeDocument document = documentService.getDocumentById(id);
                return ResponseEntity.ok(APIResponse.<EmployeeDocument>builder()
                                .success(true)
                                .message("Document found")
                                .data(document)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<APIResponse<Void>> deleteDocument(@PathVariable String id,
                        Authentication authentication) {
                EmployeeDocument document = documentService.getDocumentById(id);
                boolean isAdminOrHR = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                                                || a.getAuthority().equals("ROLE_HR")
                                                || a.getAuthority().equals("ROLE_MANAGER"));

                if (!isAdminOrHR && !document.getUploadedBy().equals(authentication.getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(APIResponse.<Void>builder()
                                                        .success(false)
                                                        .message("You can only delete your own documents")
                                                        .status(HttpStatus.FORBIDDEN.value())
                                                        .timestamp(LocalDateTime.now())
                                                        .build());
                }

                documentService.deleteDocument(id);
                return ResponseEntity.ok(APIResponse.<Void>builder()
                                .success(true)
                                .message("Document deleted successfully")
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/{id}/download")
        public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(@PathVariable String id) {
                EmployeeDocument document = documentService.getDocumentById(id);
                org.springframework.core.io.Resource resource = documentService.getFileAsResource(id);

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(document.getFileType()))
                                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + document.getFileName() + "\"")
                                .body(resource);
        }

        @GetMapping("/{id}/view")
        public ResponseEntity<org.springframework.core.io.Resource> viewDocument(@PathVariable String id) {
                EmployeeDocument document = documentService.getDocumentById(id);
                org.springframework.core.io.Resource resource = documentService.getFileAsResource(id);

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(document.getFileType()))
                                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                                "inline; filename=\"" + document.getFileName() + "\"")
                                .body(resource);
        }

        @PatchMapping("/{id}/verify")
        @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
        public ResponseEntity<APIResponse<EmployeeDocument>> verifyDocument(
                        @PathVariable String id,
                        @RequestParam String status) {
                EmployeeDocument document = documentService.verifyDocument(id, status);
                return ResponseEntity.ok(APIResponse.<EmployeeDocument>builder()
                                .success(true)
                                .message("Document status updated to " + status)
                                .data(document)
                                .status(HttpStatus.OK.value())
                                .timestamp(LocalDateTime.now())
                                .build());
        }
}

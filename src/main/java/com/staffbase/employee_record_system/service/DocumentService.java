package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.DocumentUploadDTO;
import com.staffbase.employee_record_system.entity.EmployeeDocument;
import com.staffbase.employee_record_system.repository.DocumentRepository;
import com.staffbase.employee_record_system.utils.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileValidator fileValidator;
    private final StorageService storageService;

    public EmployeeDocument uploadDocument(DocumentUploadDTO uploadDTO, String uploadedBy) {
        fileValidator.validateFile(uploadDTO.getFile());

        try {
            MultipartFile file = uploadDTO.getFile();
            String storedFileName = storageService.store(file);

            EmployeeDocument document = EmployeeDocument.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .description(uploadDTO.getDescription())
                    .filePath(storedFileName)
                    .uploadedBy(uploadedBy)
                    .uploadDate(LocalDateTime.now())
                    .build();

            return documentRepository.save(document);

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    public List<EmployeeDocument> getAllDocuments() {
        return documentRepository.findAll();
    }

    public List<EmployeeDocument> getDocumentsForUser(String username) {
        return documentRepository.findByUploadedBy(username);
    }

    public EmployeeDocument getDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id " + id));
    }

    public void deleteDocument(String id) {
        EmployeeDocument document = getDocumentById(id);
        try {
            storageService.delete(document.getFilePath());
        } catch (IOException ex) {
            System.err.println("Could not delete physical file: " + ex.getMessage());
        }
        documentRepository.deleteById(id);
    }

    public Resource getFileAsResource(String id) {
        EmployeeDocument document = getDocumentById(id);
        return storageService.load(document.getFilePath());
    }

    public EmployeeDocument verifyDocument(String id, String status) {
        EmployeeDocument document = getDocumentById(id);
        document.setStatus(status.toUpperCase());
        return documentRepository.save(document);
    }
}

package com.staffbase.employee_record_system.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class FileValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; 
    private static final List<String> ALLOWED_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/png");

    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: PDF, JPEG, PNG");
        }
    }
}




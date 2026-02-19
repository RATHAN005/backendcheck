package com.staffbase.employee_record_system.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.io.IOException;

public interface StorageService {
    String store(MultipartFile file) throws IOException;

    Resource load(String fileName);

    void delete(String fileName) throws IOException;
}




package com.staffbase.employee_record_system.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {
    private final Path rootLocation = Paths.get("/uploads").toAbsolutePath().normalize();

    public LocalStorageService() throws IOException {
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), rootLocation.resolve(fileName));
        return fileName;
    }

    @Override
    public Resource load(String fileName) {
        try {
            Path file = rootLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public void delete(String fileName) throws IOException {
        Files.deleteIfExists(rootLocation.resolve(fileName));
    }
}

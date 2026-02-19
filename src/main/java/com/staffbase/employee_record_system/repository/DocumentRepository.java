package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.EmployeeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentRepository extends MongoRepository<EmployeeDocument, String> {
    java.util.List<EmployeeDocument> findByUploadedBy(String uploadedBy);
}

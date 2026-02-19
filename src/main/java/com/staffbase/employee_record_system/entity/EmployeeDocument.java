package com.staffbase.employee_record_system.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "employee_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument {

    @Id
    private String id;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String filePath;
    private String description;
    private String uploadedBy;
    private LocalDateTime uploadDate;

    @Builder.Default
    private String status = "PENDING";
}




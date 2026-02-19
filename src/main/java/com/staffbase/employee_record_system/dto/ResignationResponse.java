package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.ResignationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ResignationResponse {
    private UUID id;
    private UUID employeeId;
    private String firstName;
    private String lastName;
    private String jobTitle;
    private LocalDate resignationDate;
    private LocalDate lastWorkingDay;
    private String reason;
    private ResignationStatus status;
    private String hrRemarks;
    private LocalDateTime createdAt;
}

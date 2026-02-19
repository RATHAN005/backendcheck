package com.staffbase.employee_record_system.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PerformanceResponse(
                UUID id,
                UUID employeeId,
                String employeeName,
                String reviewerName,
                LocalDate reviewDate,
                Integer qualityOfWork,
                Integer reliability,
                Integer technicalSkills,
                Integer teamwork,
                Integer communication,
                Double overallScore,
                String feedback,
                String goals,
                String period) {
}




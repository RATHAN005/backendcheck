package com.staffbase.employee_record_system.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PerformanceRequest(
                UUID employeeId,
                LocalDate reviewDate,
                Integer qualityOfWork,
                Integer reliability,
                Integer technicalSkills,
                Integer teamwork,
                Integer communication,
                String feedback,
                String goals,
                String period) {
}




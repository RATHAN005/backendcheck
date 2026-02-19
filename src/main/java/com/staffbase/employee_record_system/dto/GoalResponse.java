package com.staffbase.employee_record_system.dto;

import lombok.Builder;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record GoalResponse(
        UUID id,
        UUID employeeId,
        String title,
        String description,
        LocalDate targetDate,
        String status,
        Integer weightage) {
}




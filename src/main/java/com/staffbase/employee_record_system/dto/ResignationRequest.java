package com.staffbase.employee_record_system.dto;

import java.time.LocalDate;

public record ResignationRequest(
                LocalDate lastWorkingDay,
                String reason) {
}

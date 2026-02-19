package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AttendanceRequest(
        UUID employeeId,
        LocalDate date,
        LocalTime checkIn,
        LocalTime checkOut,
        AttendanceStatus status,
        String remarks) {
}




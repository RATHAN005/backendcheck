package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.LeaveType;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveApplicationRequest(
        UUID employeeId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason) {
}




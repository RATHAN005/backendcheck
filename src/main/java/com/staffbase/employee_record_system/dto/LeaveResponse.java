package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.LeaveStatus;
import com.staffbase.employee_record_system.entity.LeaveType;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveResponse(
        UUID id,
        UUID employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        String approvedBy,
        String adminRemarks) {
}




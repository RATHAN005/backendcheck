package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.PayrollStatus;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollResponse(
        UUID id,
        UUID employeeId,
        String employeeName,
        String month,
        LocalDate payDate,
        Double baseSalary,
        Double overtimePay,
        Double deductions,
        Double netPay,
        PayrollStatus status,
        String remarks) {
}




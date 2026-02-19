package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.PaymentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryRequest(
                UUID employeeId,
                Double basicSalary,
                Double hra,
                Double conveyanceAllowance,
                Double medicalAllowance,
                Double specialAllowance,
                Double bonus,
                Double pfDeduction,
                Double professionalTax,
                Double incomeTax,
                Double otherDeductions,
                LocalDate payDate,
                String payPeriod,
                PaymentStatus status) {
}




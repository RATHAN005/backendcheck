package com.staffbase.employee_record_system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "salary_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private Double basicSalary;
    private Double hra;
    private Double conveyanceAllowance;
    private Double medicalAllowance;
    private Double specialAllowance;
    private Double bonus;

    private Double pfDeduction;
    private Double professionalTax;
    private Double incomeTax;
    private Double otherDeductions;

    private Double grossSalary;
    private Double totalDeductions;
    private Double netSalary;

    private LocalDate payDate;
    private String payPeriod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}




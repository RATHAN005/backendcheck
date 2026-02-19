package com.staffbase.employee_record_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payroll_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String month;
    private LocalDate payDate;

    private Double baseSalary;
    private Double overtimePay;
    private Double deductions;
    private Double netPay;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status;

    private String remarks;
}




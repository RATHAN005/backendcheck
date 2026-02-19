package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.EmploymentType;
import com.staffbase.employee_record_system.entity.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
                UUID id,
                String firstName,
                String lastName,
                String email,
                String jobTitle,
                String departmentName,
                LocalDate hireDate,
                LocalDate dateOfBirth,
                Gender gender,
                EmploymentType employmentType,
                String phoneNumber,
                String personalEmail,
                String address,
                String nationality,
                String emergencyContactName,
                String emergencyContactPhone,
                String profilePictureUrl,
                String managerName,
                UUID managerId,
                Double baseSalary) {
}

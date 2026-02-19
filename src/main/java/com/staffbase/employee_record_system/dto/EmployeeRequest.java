package com.staffbase.employee_record_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeRequest(
                @NotBlank(message = "First name is required") String firstName,

                @NotBlank(message = "Last name is required") String lastName,

                String name,

                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

                String jobTitle,
                String department,
                UUID managerId,
                LocalDate hireDate,
                LocalDate dateOfBirth,
                String gender,
                String employmentType,
                String phoneNumber,
                String personalEmail,
                String address,
                String nationality,
                String emergencyContactName,
                String emergencyContactPhone,
                String profilePictureUrl,
                Double baseSalary) {
}




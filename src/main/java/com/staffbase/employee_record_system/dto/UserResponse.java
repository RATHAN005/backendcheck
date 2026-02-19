package com.staffbase.employee_record_system.dto;

import com.staffbase.employee_record_system.entity.Role;
import com.staffbase.employee_record_system.entity.SubscriptionPlan;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
                UUID id,
                String username,
                String email,
                String firstName,
                String lastName,
                Role role,
                SubscriptionPlan plan,
                LocalDateTime subscriptionExpiry,
                String employeeId,
                UUID employeeUUID,
                String designation,
                String phoneNumber,
                String departmentName,
                String managerName,
                LocalDate hireDate,
                LocalDate dateOfBirth,
                String gender,
                String employmentType,
                String address,
                String nationality,
                String profilePictureUrl,
                UUID managerId) {
}

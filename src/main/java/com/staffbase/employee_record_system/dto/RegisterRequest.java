package com.staffbase.employee_record_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        String username,

        @Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email,

        @NotBlank(message = "Password is required") @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters") String password,

        @NotBlank(message = "Role is required") String role) {
}




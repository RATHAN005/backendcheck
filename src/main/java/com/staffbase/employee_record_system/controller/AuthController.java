package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.LoginRequest;
import com.staffbase.employee_record_system.dto.AuthResponse;
import com.staffbase.employee_record_system.dto.RegisterRequest;
import com.staffbase.employee_record_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody com.staffbase.employee_record_system.dto.TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<com.staffbase.employee_record_system.dto.UserResponse> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {
        return ResponseEntity.ok(authService.getMe(authentication.getName()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<Void>> changePassword(
            @Valid @RequestBody com.staffbase.employee_record_system.dto.ChangePasswordRequest request,
            org.springframework.security.core.Authentication authentication) {

        authService.changePassword(authentication.getName(), request);

        return ResponseEntity.ok(com.staffbase.employee_record_system.dto.APIResponse.<Void>builder()
                .success(true)
                .message("Password changed successfully")
                .status(200)
                .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<Void>> forgotPassword(
            @Valid @RequestBody com.staffbase.employee_record_system.dto.ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request.email());
        return ResponseEntity.ok(com.staffbase.employee_record_system.dto.APIResponse.<Void>builder()
                .success(true)
                .message("Password reset initiated. Check your email or backend console.")
                .status(200)
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<com.staffbase.employee_record_system.dto.APIResponse<Void>> resetPassword(
            @Valid @RequestBody com.staffbase.employee_record_system.dto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(com.staffbase.employee_record_system.dto.APIResponse.<Void>builder()
                .success(true)
                .message("Password reset successfully")
                .status(200)
                .build());
    }
}




package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.LoginRequest;
import com.staffbase.employee_record_system.dto.AuthResponse;
import com.staffbase.employee_record_system.dto.RegisterRequest;
import com.staffbase.employee_record_system.entity.Role;
import com.staffbase.employee_record_system.entity.User;
import com.staffbase.employee_record_system.repository.UserRepository;
import com.staffbase.employee_record_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final com.staffbase.employee_record_system.repository.EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public AuthResponse register(RegisterRequest request) {
        String username = request.username();
        if (username == null || username.isEmpty()) {
            username = request.email().split("@")[0];
        }

        var user = User.builder()
                .username(username)
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? Role.valueOf(request.role().toUpperCase()) : Role.EMPLOYEE)
                .build();

        userRepository.save(user);

        var employee = com.staffbase.employee_record_system.entity.Employee.builder()
                .firstName(username)
                .lastName("")
                .personalEmail(request.email())
                .jobTitle(request.role() != null && request.role().equalsIgnoreCase("ADMIN") ? "System Administrator"
                        : "Employee")
                .user(user)
                .build();

        employeeRepository.save(employee);

        var token = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        return new AuthResponse(token, refreshToken.getToken());
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));
        var user = userRepository.findByEmail(request.email())
                .orElseThrow();

        if (user.getRole() == Role.HR || user.getRole() == Role.ADMIN) {
            auditLogService.logAction(user.getRole() + "_LOGIN", user.getEmail(),
                    user.getRole() + " user logged into the system");
        }

        var token = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        return new AuthResponse(token, refreshToken.getToken());
    }

    public AuthResponse refreshToken(com.staffbase.employee_record_system.dto.TokenRefreshRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(com.staffbase.employee_record_system.entity.RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return new AuthResponse(token, request.refreshToken());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    public com.staffbase.employee_record_system.dto.UserResponse getMe(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            String generatedUsername = email.split("@")[0];
            user.setUsername(generatedUsername);
            userRepository.save(user);
        }

        String firstName = "System";
        String lastName = "User";
        String employeeId = "EMP-" + user.getId().toString().substring(0, 8).toUpperCase();
        String designation = user.getRole().name();

        String phoneNumber = null;
        String departmentName = "All Access";
        String managerName = "None";
        java.time.LocalDate hireDate = null;
        java.time.LocalDate dateOfBirth = null;
        String gender = null;
        String employmentType = null;
        String address = null;
        String nationality = null;
        String profilePictureUrl = null;
        java.util.UUID managerId = null;

        var employeeOpt = employeeRepository.findByUserEmail(email);
        java.util.UUID employeeUUID = null;
        if (employeeOpt.isPresent()) {
            var employee = employeeOpt.get();
            firstName = employee.getFirstName();
            lastName = employee.getLastName();
            designation = employee.getJobTitle() != null ? employee.getJobTitle() : designation;
            employeeId = "EMP-" + employee.getId().toString().substring(0, 8).toUpperCase();
            employeeUUID = employee.getId();
            phoneNumber = employee.getPhoneNumber();
            departmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : "N/A";
            managerName = employee.getManager() != null
                    ? (employee.getManager().getFirstName() + " " + employee.getManager().getLastName())
                    : "None";
            hireDate = employee.getHireDate();
            dateOfBirth = employee.getDateOfBirth();
            gender = employee.getGender() != null ? employee.getGender().name() : null;
            employmentType = employee.getEmploymentType() != null ? employee.getEmploymentType().name() : null;
            address = employee.getAddress();
            nationality = employee.getNationality();
            profilePictureUrl = employee.getProfilePictureUrl();
            managerId = employee.getManager() != null ? employee.getManager().getId() : null;
        } else if (user.getRole() == Role.ADMIN) {
            firstName = user.getUsername() != null ? user.getUsername() : user.getEmail().split("@")[0];
            lastName = "";
            designation = "System Administrator";
        }

        return new com.staffbase.employee_record_system.dto.UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                firstName,
                lastName,
                user.getRole(),
                user.getPlan(),
                user.getSubscriptionExpiry(),
                employeeId,
                employeeUUID,
                designation,
                phoneNumber,
                departmentName,
                managerName,
                hireDate,
                dateOfBirth,
                gender,
                employmentType,
                address,
                nationality,
                profilePictureUrl,
                managerId);
    }

    public void changePassword(String email, com.staffbase.employee_record_system.dto.ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private final com.staffbase.employee_record_system.repository.PasswordResetTokenRepository tokenRepository;

    public void initiatePasswordReset(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = java.util.UUID.randomUUID().toString();
        var resetToken = com.staffbase.employee_record_system.entity.PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1))
                .build();

        tokenRepository.save(resetToken);

        System.out.println("==========================================");
        System.out.println("PASSWORD RESET REQUEST FOR: " + email);
        System.out.println("TOKEN: " + token);
        System.out.println("LINK: http://localhost:5173/reset-password?token=" + token);
        System.out.println("==========================================");
    }

    public void resetPassword(com.staffbase.employee_record_system.dto.ResetPasswordRequest request) {
        var resetToken = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("Token has expired");
        }

        var user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}

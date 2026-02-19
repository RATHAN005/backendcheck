package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.GoalRequest;
import com.staffbase.employee_record_system.dto.GoalResponse;
import com.staffbase.employee_record_system.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<GoalResponse>>> getMyGoals(Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<List<GoalResponse>>builder()
                .success(true)
                .message("My goals retrieved successfully")
                .data(goalService.getGoalsByEmail(authentication.getName()))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<GoalResponse>> createGoal(
            @RequestBody GoalRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<GoalResponse>builder()
                .success(true)
                .message("Goal created successfully")
                .data(goalService.createGoal(request, authentication.getName()))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<APIResponse<List<GoalResponse>>> getEmployeeGoals(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(APIResponse.<List<GoalResponse>>builder()
                .success(true)
                .message("Goals retrieved successfully")
                .data(goalService.getEmployeeGoals(employeeId))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/{goalId}/status")
    public ResponseEntity<APIResponse<GoalResponse>> updateStatus(
            @PathVariable UUID goalId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(APIResponse.<GoalResponse>builder()
                .success(true)
                .message("Goal status updated")
                .data(goalService.updateGoalStatus(goalId, body.get("status")))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<APIResponse<Void>> deleteGoal(@PathVariable UUID goalId) {
        goalService.deleteGoal(goalId);
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .success(true)
                .message("Goal deleted")
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }
}

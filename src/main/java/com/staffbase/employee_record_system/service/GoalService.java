package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.GoalRequest;
import com.staffbase.employee_record_system.dto.GoalResponse;
import com.staffbase.employee_record_system.entity.Employee;
import com.staffbase.employee_record_system.entity.Goal;
import com.staffbase.employee_record_system.entity.GoalStatus;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.staffbase.employee_record_system.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    private final GoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;

    public GoalResponse createGoal(GoalRequest request, String userEmail) {
        Employee employee;
        if (request.employeeId() != null) {
            employee = employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
        } else {
            employee = employeeRepository.findByUserEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Employee profile not found for this account"));
        }

        GoalStatus status = GoalStatus.IN_PROGRESS;
        if (request.status() != null) {
            try {
                status = GoalStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Goal goal = Goal.builder()
                .employee(employee)
                .title(request.title())
                .description(request.description())
                .targetDate(request.targetDate())
                .status(status)
                .weightage(request.weightage())
                .build();

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved);
    }

    public List<GoalResponse> getEmployeeGoals(UUID employeeId) {
        return goalRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<GoalResponse> getGoalsByEmail(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for this account"));
        return getEmployeeGoals(employee.getId());
    }

    public GoalResponse updateGoalStatus(UUID goalId, String status) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        try {
            goal.setStatus(GoalStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
        return mapToResponse(goalRepository.save(goal));
    }

    public void deleteGoal(UUID goalId) {
        goalRepository.deleteById(goalId);
    }

    private GoalResponse mapToResponse(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .employeeId(goal.getEmployee().getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus() != null ? goal.getStatus().name() : "IN_PROGRESS")
                .weightage(goal.getWeightage())
                .build();
    }
}

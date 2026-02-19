package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.PerformanceRequest;
import com.staffbase.employee_record_system.dto.PerformanceResponse;
import com.staffbase.employee_record_system.entity.*;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.staffbase.employee_record_system.repository.PerformanceReviewRepository;
import com.staffbase.employee_record_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceService {
        private final PerformanceReviewRepository reviewRepository;
        private final EmployeeRepository employeeRepository;
        private final UserRepository userRepository;
        private final AuditLogService auditLogService;

        public PerformanceResponse addReview(PerformanceRequest request, String reviewerEmail) {
                Employee employee = employeeRepository.findById(request.employeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                User reviewer = userRepository.findByEmail(reviewerEmail)
                                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

                double overall = (request.qualityOfWork() + request.reliability() + request.technicalSkills() +
                                request.teamwork() + request.communication()) / 5.0;

                PerformanceReview review = PerformanceReview.builder()
                                .employee(employee)
                                .reviewer(reviewer)
                                .reviewDate(request.reviewDate())
                                .qualityOfWork(request.qualityOfWork())
                                .reliability(request.reliability())
                                .technicalSkills(request.technicalSkills())
                                .teamwork(request.teamwork())
                                .communication(request.communication())
                                .overallScore(overall)
                                .feedback(request.feedback())
                                .goals(request.goals())
                                .period(request.period())
                                .build();

                auditLogService.logAction("PERFORMANCE_REVIEW", reviewerEmail,
                                "Employee: " + employee.getFirstName() + " " + employee.getLastName() + ", Score: "
                                                + String.format("%.1f", overall));

                return mapToResponse(reviewRepository.save(review));
        }

        public List<PerformanceResponse> getEmployeeReviews(UUID employeeId) {
                return reviewRepository.findByEmployeeId(employeeId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        private PerformanceResponse mapToResponse(PerformanceReview review) {
                return new PerformanceResponse(
                                review.getId(),
                                review.getEmployee().getId(),
                                review.getEmployee().getFirstName() + " " + review.getEmployee().getLastName(),
                                review.getReviewer().getUsername(),
                                review.getReviewDate(),
                                review.getQualityOfWork(),
                                review.getReliability(),
                                review.getTechnicalSkills(),
                                review.getTeamwork(),
                                review.getCommunication(),
                                review.getOverallScore(),
                                review.getFeedback(),
                                review.getGoals(),
                                review.getPeriod());
        }
}




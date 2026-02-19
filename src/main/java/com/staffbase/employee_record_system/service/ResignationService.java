package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.ResignationRequest;
import com.staffbase.employee_record_system.dto.ResignationResponse;
import com.staffbase.employee_record_system.entity.Employee;
import com.staffbase.employee_record_system.entity.Resignation;
import com.staffbase.employee_record_system.entity.ResignationStatus;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.staffbase.employee_record_system.repository.ResignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResignationService {

    private final ResignationRepository resignationRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ResignationResponse applyResignation(String email, ResignationRequest request) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (resignationRepository.findByEmployeeUserEmailAndStatus(email, ResignationStatus.PENDING).isPresent()) {
            throw new RuntimeException("A resignation request is already pending");
        }

        Resignation resignation = Resignation.builder()
                .employee(employee)
                .resignationDate(LocalDate.now())
                .lastWorkingDay(request.lastWorkingDay())
                .reason(request.reason())
                .status(ResignationStatus.PENDING)
                .build();

        Resignation saved = resignationRepository.save(resignation);
        auditLogService.logAction("RESIGNATION_APPLIED",
                employee.getFirstName() + " " + employee.getLastName(),
                "Last Working Day: " + request.lastWorkingDay());

        return mapToResponse(saved);
    }

    public List<ResignationResponse> getAllResignations() {
        return resignationRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public ResignationResponse updateResignationStatus(UUID id, ResignationStatus status, String remarks) {
        Resignation resignation = resignationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resignation record not found"));

        resignation.setStatus(status);
        resignation.setHrRemarks(remarks);

        Resignation saved = resignationRepository.save(resignation);
        auditLogService.logAction("RESIGNATION_STATUS_UPDATED",
                resignation.getEmployee().getFirstName() + " " + resignation.getEmployee().getLastName(),
                "Status: " + status);

        return mapToResponse(saved);
    }

    public List<ResignationResponse> getMyResignations(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return resignationRepository.findByEmployeeId(employee.getId()).stream().map(this::mapToResponse).toList();
    }

    private ResignationResponse mapToResponse(Resignation resignation) {
        return ResignationResponse.builder()
                .id(resignation.getId())
                .employeeId(resignation.getEmployee().getId())
                .firstName(resignation.getEmployee().getFirstName())
                .lastName(resignation.getEmployee().getLastName())
                .jobTitle(resignation.getEmployee().getJobTitle())
                .resignationDate(resignation.getResignationDate())
                .lastWorkingDay(resignation.getLastWorkingDay())
                .reason(resignation.getReason())
                .status(resignation.getStatus())
                .hrRemarks(resignation.getHrRemarks())
                .createdAt(resignation.getCreatedAt())
                .build();
    }
}

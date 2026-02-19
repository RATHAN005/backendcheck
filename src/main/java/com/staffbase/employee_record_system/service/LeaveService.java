package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.LeaveApplicationRequest;
import com.staffbase.employee_record_system.dto.LeaveResponse;
import com.staffbase.employee_record_system.entity.*;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.staffbase.employee_record_system.repository.LeaveRequestRepository;
import com.staffbase.employee_record_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class LeaveService {
        private final LeaveRequestRepository leaveRequestRepository;
        private final EmployeeRepository employeeRepository;
        private final UserRepository userRepository;
        private final com.staffbase.employee_record_system.repository.LeaveBalanceRepository leaveBalanceRepository;
        private final AuditLogService auditLogService;

        public LeaveResponse applyForLeave(LeaveApplicationRequest request, String userEmail) {
                // Resolve employee from either the authenticated email or the provided ID
                Employee employee = employeeRepository.findByUserEmail(userEmail)
                                .orElseGet(() -> resolveEmployee(request.employeeId()));

                if (request.startDate() == null || request.endDate() == null) {
                        throw new RuntimeException("Start date and end date are required");
                }

                if (request.startDate().isAfter(request.endDate())) {
                        throw new RuntimeException("Start date cannot be after end date");
                }

                var balance = leaveBalanceRepository.findByEmployeeIdAndLeaveType(employee.getId(), request.leaveType())
                                .orElseGet(() -> {
                                        int defaultEntitled = switch (request.leaveType()) {
                                                case ANNUAL -> 20;
                                                case SICK -> 10;
                                                case CASUAL -> 8;
                                                case MATERNITY -> 90;
                                                case PATERNITY -> 14;
                                                case UNPAID -> 365;
                                                default -> 0;
                                        };
                                        LeaveBalance newBalance = LeaveBalance.builder()
                                                        .employee(employee)
                                                        .leaveType(request.leaveType())
                                                        .totalEntitled(defaultEntitled)
                                                        .used(0)
                                                        .pending(0)
                                                        .build();
                                        return leaveBalanceRepository.save(newBalance);
                                });

                long days = java.time.temporal.ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
                // Restriction removed as per user request: Allow submission even if balance is
                // insufficient.
                /*
                 * if (balance.getRemaining() < days) {
                 * throw new RuntimeException("Insufficient leave balance. Remaining: " +
                 * balance.getRemaining());
                 * }
                 */

                LeaveRequest leaveRequest = LeaveRequest.builder()
                                .employee(employee)
                                .leaveType(request.leaveType())
                                .startDate(request.startDate())
                                .endDate(request.endDate())
                                .reason(request.reason())
                                .status(LeaveStatus.PENDING)
                                .build();

                balance.setPending(balance.getPending() + (int) days);
                leaveBalanceRepository.save(balance);

                return mapToResponse(leaveRequestRepository.save(leaveRequest));
        }

        public LeaveResponse approveOrRejectLeave(UUID leaveId, LeaveStatus status, String remarks, String adminEmail) {
                LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                                .orElseThrow(() -> new RuntimeException("Leave request not found"));

                if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
                        throw new RuntimeException("Leave request already processed");
                }

                User admin = userRepository.findByEmail(adminEmail)
                                .orElseThrow(() -> new RuntimeException("Admin user not found"));

                long days = java.time.temporal.ChronoUnit.DAYS.between(leaveRequest.getStartDate(),
                                leaveRequest.getEndDate()) + 1;
                var balance = leaveBalanceRepository
                                .findByEmployeeIdAndLeaveType(leaveRequest.getEmployee().getId(),
                                                leaveRequest.getLeaveType())
                                .orElseGet(() -> {
                                        int defaultEntitled = switch (leaveRequest.getLeaveType()) {
                                                case ANNUAL -> 20;
                                                case SICK -> 10;
                                                case CASUAL -> 8;
                                                case MATERNITY -> 90;
                                                case PATERNITY -> 14;
                                                case UNPAID -> 365;
                                                default -> 0;
                                        };
                                        LeaveBalance newBalance = LeaveBalance.builder()
                                                        .employee(leaveRequest.getEmployee())
                                                        .leaveType(leaveRequest.getLeaveType())
                                                        .totalEntitled(defaultEntitled)
                                                        .used(0)
                                                        .pending(0)
                                                        .build();
                                        return leaveBalanceRepository.save(newBalance);
                                });

                if (status == LeaveStatus.APPROVED) {
                        balance.setPending(balance.getPending() - (int) days);
                        balance.setUsed(balance.getUsed() + (int) days);
                } else if (status == LeaveStatus.REJECTED) {
                        balance.setPending(balance.getPending() - (int) days);
                }

                leaveBalanceRepository.save(balance);
                leaveRequest.setStatus(status);
                leaveRequest.setAdminRemarks(remarks);
                leaveRequest.setApprovedBy(admin);

                auditLogService.logAction("LEAVE_ACTION", adminEmail,
                                "Leave ID: " + leaveId + ", Status: " + status + ", Days: " + days);

                return mapToResponse(leaveRequestRepository.save(leaveRequest));
        }

        public List<LeaveResponse> getEmployeeLeaves(UUID id) {
                Employee employee = resolveEmployee(id);
                return leaveRequestRepository.findByEmployeeId(employee.getId()).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<LeaveResponse> getAllLeaves() {
                return leaveRequestRepository.findAll().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<com.staffbase.employee_record_system.entity.LeaveBalance> getLeaveBalances(UUID id) {
                Employee employee = resolveEmployee(id);
                List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeId(employee.getId());

                // If no balances exist, initialize defaults for all leave types
                if (balances.isEmpty()) {
                        for (LeaveType type : LeaveType.values()) {
                                int defaultEntitled = switch (type) {
                                        case ANNUAL -> 20;
                                        case SICK -> 10;
                                        case CASUAL -> 8;
                                        case MATERNITY -> 90;
                                        case PATERNITY -> 14;
                                        case UNPAID -> 365;
                                        default -> 0;
                                };

                                LeaveBalance newBalance = LeaveBalance.builder()
                                                .employee(employee)
                                                .leaveType(type)
                                                .totalEntitled(defaultEntitled)
                                                .used(0)
                                                .pending(0)
                                                .build();
                                leaveBalanceRepository.save(newBalance);
                        }
                        return leaveBalanceRepository.findByEmployeeId(employee.getId());
                }

                return balances;
        }

        private Employee resolveEmployee(UUID id) {
                if (id == null) {
                        throw new RuntimeException("ID is required to resolve employee");
                }
                // Try as employee ID
                return employeeRepository.findById(id)
                                .or(() -> employeeRepository.findByUserId(id))
                                .orElseThrow(() -> new RuntimeException("Employee not found for ID: " + id));
        }

        private LeaveResponse mapToResponse(LeaveRequest leave) {
                return new LeaveResponse(
                                leave.getId(),
                                leave.getEmployee().getId(),
                                leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName(),
                                leave.getLeaveType(),
                                leave.getStartDate(),
                                leave.getEndDate(),
                                leave.getReason(),
                                leave.getStatus(),
                                leave.getApprovedBy() != null ? leave.getApprovedBy().getUsername() : null,
                                leave.getAdminRemarks());
        }
}

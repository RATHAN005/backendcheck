package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.AttendanceRequest;
import com.staffbase.employee_record_system.dto.AttendanceResponse;
import com.staffbase.employee_record_system.entity.Attendance;
import com.staffbase.employee_record_system.entity.AttendanceStatus;
import com.staffbase.employee_record_system.entity.Employee;
import com.staffbase.employee_record_system.repository.AttendanceRepository;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {
        private final AttendanceRepository attendanceRepository;
        private final EmployeeRepository employeeRepository;
        private final AuditLogService auditLogService;

        public AttendanceResponse checkInByEmail(String email) {
                Employee employee = employeeRepository.findByUserEmail(email)
                                .orElseThrow(() -> new RuntimeException("Employee profile not found for this account"));
                return checkIn(employee.getId());
        }

        public AttendanceResponse checkOutByEmail(String email) {
                Employee employee = employeeRepository.findByUserEmail(email)
                                .orElseThrow(() -> new RuntimeException("Employee profile not found for this account"));
                return checkOut(employee.getId());
        }

        public List<AttendanceResponse> getAttendanceByEmail(String email) {
                Employee employee = employeeRepository.findByUserEmail(email)
                                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
                return getEmployeeAttendance(employee.getId());
        }

        public AttendanceResponse checkIn(UUID employeeId) {
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                LocalDate today = LocalDate.now();
                if (attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today).isPresent()) {
                        throw new RuntimeException("Already checked in for today");
                }

                LocalTime now = LocalTime.now();
                AttendanceStatus status = now.isAfter(LocalTime.of(9, 30)) ? AttendanceStatus.LATE
                                : AttendanceStatus.PRESENT;

                Attendance attendance = Attendance.builder()
                                .employee(employee)
                                .date(today)
                                .checkIn(now)
                                .status(status)
                                .build();

                Attendance saved = attendanceRepository.save(attendance);
                auditLogService.logAction("CHECK_IN", employee.getFirstName() + " " + employee.getLastName(),
                                "Time: " + now);
                return mapToResponse(saved);
        }

        public AttendanceResponse checkOut(UUID employeeId) {
                LocalDate today = LocalDate.now();
                Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

                if (attendance.getCheckOut() != null) {
                        throw new RuntimeException("Already checked out for today");
                }

                LocalTime now = LocalTime.now();
                attendance.setCheckOut(now);

                if (attendance.getCheckIn() != null) {
                        long minutes = Duration.between(attendance.getCheckIn(), now).toMinutes();
                        double hours = minutes / 60.0;
                        attendance.setWorkHours(hours);
                }

                Attendance saved = attendanceRepository.save(attendance);
                auditLogService.logAction("CHECK_OUT",
                                attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName(),
                                "Time: " + now + ", Total Hours: " + String.format("%.2f", attendance.getWorkHours()));

                return mapToResponse(saved);
        }

        public AttendanceResponse logAttendance(AttendanceRequest request) {
                Employee employee = employeeRepository.findById(request.employeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                Attendance attendance = Attendance.builder()
                                .employee(employee)
                                .date(request.date())
                                .checkIn(request.checkIn())
                                .checkOut(request.checkOut())
                                .status(request.status())
                                .remarks(request.remarks())
                                .build();

                if (request.checkIn() != null && request.checkOut() != null) {
                        long minutes = Duration.between(request.checkIn(), request.checkOut()).toMinutes();
                        attendance.setWorkHours(minutes / 60.0);
                }

                return mapToResponse(attendanceRepository.save(attendance));
        }

        public List<AttendanceResponse> getEmployeeAttendance(UUID employeeId) {
                return attendanceRepository.findByEmployeeId(employeeId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public String generateAttendanceCsvByEmail(String email) {
                Employee employee = employeeRepository.findByUserEmail(email)
                                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
                List<Attendance> records = attendanceRepository.findByEmployeeId(employee.getId());

                StringBuilder csv = new StringBuilder();
                csv.append("Date,Check In,Check Out,Work Hours,Status,Remarks\n");

                for (Attendance rec : records) {
                        csv.append(rec.getDate()).append(",")
                                        .append(rec.getCheckIn() != null ? rec.getCheckIn() : "-").append(",")
                                        .append(rec.getCheckOut() != null ? rec.getCheckOut() : "-").append(",")
                                        .append(rec.getWorkHours() != null ? String.format("%.2f", rec.getWorkHours())
                                                        : "0.00")
                                        .append(",")
                                        .append(rec.getStatus()).append(",")
                                        .append(rec.getRemarks() != null ? rec.getRemarks() : "Web Punch").append("\n");
                }

                return csv.toString();
        }

        private AttendanceResponse mapToResponse(Attendance attendance) {
                return new AttendanceResponse(
                                attendance.getId(),
                                attendance.getEmployee().getId(),
                                attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName(),
                                attendance.getDate(),
                                attendance.getCheckIn(),
                                attendance.getCheckOut(),
                                attendance.getStatus(),
                                attendance.getRemarks(),
                                attendance.getWorkHours());
        }
}

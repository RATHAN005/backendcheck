package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.AttendanceRequest;
import com.staffbase.employee_record_system.dto.AttendanceResponse;
import com.staffbase.employee_record_system.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<APIResponse<AttendanceResponse>> checkIn(
            @RequestParam(required = false) UUID employeeId,
            Authentication authentication) {
        AttendanceResponse result;
        if (employeeId != null) {
            result = attendanceService.checkIn(employeeId);
        } else {
            result = attendanceService.checkInByEmail(authentication.getName());
        }
        return ResponseEntity.ok(APIResponse.<AttendanceResponse>builder()
                .success(true)
                .message("Checked in successfully")
                .data(result)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/check-out")
    public ResponseEntity<APIResponse<AttendanceResponse>> checkOut(
            @RequestParam(required = false) UUID employeeId,
            Authentication authentication) {
        AttendanceResponse result;
        if (employeeId != null) {
            result = attendanceService.checkOut(employeeId);
        } else {
            result = attendanceService.checkOutByEmail(authentication.getName());
        }
        return ResponseEntity.ok(APIResponse.<AttendanceResponse>builder()
                .success(true)
                .message("Checked out successfully")
                .data(result)
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<AttendanceResponse>> logAttendance(@RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(APIResponse.<AttendanceResponse>builder()
                .success(true)
                .message("Attendance record logged")
                .data(attendanceService.logAttendance(request))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<APIResponse<List<AttendanceResponse>>> getEmployeeAttendance(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(APIResponse.<List<AttendanceResponse>>builder()
                .success(true)
                .message("Attendance history retrieved")
                .data(attendanceService.getEmployeeAttendance(employeeId))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<AttendanceResponse>>> getMyAttendance(Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<List<AttendanceResponse>>builder()
                .success(true)
                .message("Attendance history retrieved")
                .data(attendanceService.getAttendanceByEmail(authentication.getName()))
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/my/csv")
    public ResponseEntity<byte[]> downloadMyAttendanceCSV(Authentication authentication) {
        String csvData = attendanceService.generateAttendanceCsvByEmail(authentication.getName());
        byte[] bytes = csvData.getBytes();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=attendance_history.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}

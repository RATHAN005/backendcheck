package com.staffbase.employee_record_system.controller;

import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.dto.PayrollResponse;
import com.staffbase.employee_record_system.dto.SalaryRequest;
import com.staffbase.employee_record_system.dto.SalaryResponse;
import com.staffbase.employee_record_system.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

        private final PayrollService payrollService;

        @PostMapping("/generate")
        @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
        public ResponseEntity<APIResponse<PayrollResponse>> generatePayroll(
                        @RequestParam UUID employeeId,
                        @RequestParam String month) {
                return ResponseEntity.ok(APIResponse.<PayrollResponse>builder()
                                .success(true)
                                .message("Payroll generated successfully")
                                .data(payrollService.generatePayroll(employeeId, month))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/calculate")
        @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
        public ResponseEntity<APIResponse<SalaryResponse>> calculateSalaryPreview(
                        @RequestParam UUID employeeId,
                        @RequestParam Double basicSalary) {
                return ResponseEntity.ok(APIResponse.<SalaryResponse>builder()
                                .success(true)
                                .message("Salary preview calculated")
                                .data(payrollService.calculateSalaryPreview(employeeId, basicSalary))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @PostMapping("/process")
        @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
        public ResponseEntity<APIResponse<SalaryResponse>> processPayroll(
                        @RequestBody SalaryRequest request) {
                return ResponseEntity.ok(APIResponse.<SalaryResponse>builder()
                                .success(true)
                                .message("Salary processed successfully")
                                .data(payrollService.processSalary(request))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/my")
        public ResponseEntity<APIResponse<List<PayrollResponse>>> getMyPayroll(Authentication authentication) {
                return ResponseEntity.ok(APIResponse.<List<PayrollResponse>>builder()
                                .success(true)
                                .message("Payroll history retrieved")
                                .data(payrollService.getPayrollByEmail(authentication.getName()))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/employee/{employeeId}")
        public ResponseEntity<APIResponse<List<PayrollResponse>>> getEmployeePayroll(@PathVariable UUID employeeId) {
                return ResponseEntity.ok(APIResponse.<List<PayrollResponse>>builder()
                                .success(true)
                                .message("Payroll history retrieved")
                                .data(payrollService.getEmployeePayroll(employeeId))
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
        public ResponseEntity<APIResponse<List<PayrollResponse>>> getAllPayroll() {
                return ResponseEntity.ok(APIResponse.<List<PayrollResponse>>builder()
                                .success(true)
                                .message("All payroll records retrieved")
                                .data(payrollService.getAllPayroll())
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        }

        @GetMapping("/download/{payrollId}")
        public ResponseEntity<byte[]> downloadPayslip(@PathVariable UUID payrollId) {
                byte[] pdf = payrollService.generatePayslipPdf(payrollId);
                return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=payslip_" + payrollId + ".pdf")
                                .header("Content-Type", "application/pdf")
                                .body(pdf);
        }

        @GetMapping("/annual-statement/{employeeId}/{year}")
        public ResponseEntity<byte[]> downloadAnnualStatement(@PathVariable UUID employeeId, @PathVariable int year) {
                byte[] pdf = payrollService.generateAnnualStatementPdf(employeeId, year);
                return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=annual_statement_" + year + ".pdf")
                                .header("Content-Type", "application/pdf")
                                .body(pdf);
        }

        @GetMapping("/my/annual-statement/{year}")
        public ResponseEntity<byte[]> downloadMyAnnualStatement(@PathVariable int year, Authentication authentication) {
                byte[] pdf = payrollService.generateAnnualStatementByEmail(authentication.getName(), year);
                return ResponseEntity.ok()
                                .header("Content-Disposition", "attachment; filename=annual_statement_" + year + ".pdf")
                                .header("Content-Type", "application/pdf")
                                .body(pdf);
        }
}

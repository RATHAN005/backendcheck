package com.staffbase.employee_record_system.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.staffbase.employee_record_system.dto.PayrollResponse;
import com.staffbase.employee_record_system.entity.Attendance;
import com.staffbase.employee_record_system.entity.Employee;
import com.staffbase.employee_record_system.entity.Payroll;
import com.staffbase.employee_record_system.entity.PayrollStatus;
import com.staffbase.employee_record_system.repository.AttendanceRepository;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.staffbase.employee_record_system.repository.PayrollRepository;
import com.staffbase.employee_record_system.repository.SalaryRecordRepository;
import com.staffbase.employee_record_system.dto.SalaryRequest;
import com.staffbase.employee_record_system.dto.SalaryResponse;
import com.staffbase.employee_record_system.entity.SalaryRecord;
import com.staffbase.employee_record_system.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

        private final PayrollRepository payrollRepository;
        private final EmployeeRepository employeeRepository;
        private final AttendanceRepository attendanceRepository;
        private final SalaryRecordRepository salaryRecordRepository;
        private final AuditLogService auditLogService;

        public PayrollResponse generatePayroll(UUID employeeId, String monthStr) {

                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                YearMonth yearMonth = YearMonth.parse(monthStr);
                LocalDate start = yearMonth.atDay(1);
                LocalDate end = yearMonth.atEndOfMonth();

                List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start,
                                end);

                double totalHours = attendances.stream()
                                .filter(a -> a.getWorkHours() != null)
                                .mapToDouble(Attendance::getWorkHours)
                                .sum();

                double baseSalary = employee.getBaseSalary() != null ? employee.getBaseSalary() : 0.0;

                double overtimeHours = Math.max(0, totalHours - 160);
                double hourlyRate = baseSalary / 160.0;
                double overtimePay = overtimeHours * hourlyRate * 1.5;

                double deductions = baseSalary * 0.1;
                double netPay = baseSalary + overtimePay - deductions;

                Payroll payroll = Payroll.builder()
                                .employee(employee)
                                .month(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                                .payDate(LocalDate.now())
                                .baseSalary(baseSalary)
                                .overtimePay(overtimePay)
                                .deductions(deductions)
                                .netPay(netPay)
                                .status(PayrollStatus.PENDING)
                                .remarks("Automated calculation based on " + String.format("%.1f", totalHours)
                                                + " work hours.")
                                .build();

                Payroll saved = payrollRepository.save(payroll);

                auditLogService.logAction("PAYROLL_GENERATED", "SYSTEM",
                                "Employee: " + employee.getFirstName() + " " + employee.getLastName() + ", Month: "
                                                + payroll.getMonth());

                return mapToResponse(saved);
        }

        public List<PayrollResponse> getEmployeePayroll(UUID employeeId) {
                return payrollRepository.findByEmployeeId(employeeId).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<PayrollResponse> getPayrollByEmail(String email) {
                Employee employee = employeeRepository.findByUserEmail(email)
                                .orElseThrow(() -> new RuntimeException("Employee profile not found for this account"));
                return getEmployeePayroll(employee.getId());
        }

        public byte[] generateAnnualStatementByEmail(String email, int year) {
                Employee employee = employeeRepository.findByUserEmail(email)
                                .orElseThrow(() -> new RuntimeException("Employee profile not found for this account"));
                return generateAnnualStatementPdf(employee.getId(), year);
        }

        public SalaryResponse calculateSalaryPreview(UUID employeeId, Double basicSalary) {
                if (basicSalary == null) {
                        basicSalary = 0.0;
                }
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                double hra = basicSalary * 0.40;
                double conveyance = 1600.0;
                double medical = 1250.0;
                double special = 2000.0;
                double bonus = 0.0;

                double grossSalary = basicSalary + hra + conveyance + medical + special + bonus;

                double pf = Math.min(basicSalary * 0.12, 1800.0);
                double professionalTax = 200.0;

                // Simplified tax calculation for preview
                double taxableIncome = grossSalary - pf - professionalTax;
                double incomeTax = 0.0;
                if (taxableIncome * 12 > 500000) {
                        incomeTax = (taxableIncome * 12 - 500000) * 0.10 / 12; // Monthly tax
                }

                double otherDeductions = 0.0;
                double totalDeductions = pf + professionalTax + incomeTax + otherDeductions;
                double netSalary = grossSalary - totalDeductions;

                return new SalaryResponse(
                                null,
                                employee.getId(),
                                employee.getFirstName() + " " + employee.getLastName(),
                                basicSalary,
                                hra,
                                conveyance,
                                medical,
                                special,
                                bonus,
                                pf,
                                professionalTax,
                                incomeTax,
                                otherDeductions,
                                grossSalary,
                                totalDeductions,
                                netSalary,
                                LocalDate.now(),
                                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                PaymentStatus.PENDING);
        }

        public SalaryResponse processSalary(SalaryRequest request) {
                Employee employee = employeeRepository.findById(request.employeeId())
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                double grossSalary = request.basicSalary() + request.hra() + request.conveyanceAllowance()
                                + request.medicalAllowance() + request.specialAllowance() + request.bonus();
                double totalDeductions = request.pfDeduction() + request.professionalTax() + request.incomeTax()
                                + request.otherDeductions();
                double netSalary = grossSalary - totalDeductions;

                SalaryRecord salaryRecord = SalaryRecord.builder()
                                .employee(employee)
                                .basicSalary(request.basicSalary())
                                .hra(request.hra())
                                .conveyanceAllowance(request.conveyanceAllowance())
                                .medicalAllowance(request.medicalAllowance())
                                .specialAllowance(request.specialAllowance())
                                .bonus(request.bonus())
                                .pfDeduction(request.pfDeduction())
                                .professionalTax(request.professionalTax())
                                .incomeTax(request.incomeTax())
                                .otherDeductions(request.otherDeductions())
                                .grossSalary(grossSalary)
                                .totalDeductions(totalDeductions)
                                .netSalary(netSalary)
                                .payPeriod(request.payPeriod())
                                .payDate(request.payDate())
                                .status(request.status())
                                .build();

                SalaryRecord saved = salaryRecordRepository.save(salaryRecord);

                auditLogService.logAction("SALARY_PROCESSED", "SYSTEM",
                                "Processed salary for " + employee.getFirstName() + ", Amount: " + netSalary);

                return mapToSalaryResponse(saved);
        }

        public List<SalaryResponse> getAllSalaries() {
                return salaryRecordRepository.findAll().stream()
                                .map(this::mapToSalaryResponse)
                                .collect(Collectors.toList());
        }

        private SalaryResponse mapToSalaryResponse(SalaryRecord record) {
                return new SalaryResponse(
                                record.getId(),
                                record.getEmployee().getId(),
                                record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName(),
                                record.getBasicSalary(),
                                record.getHra(),
                                record.getConveyanceAllowance(),
                                record.getMedicalAllowance(),
                                record.getSpecialAllowance(),
                                record.getBonus(),
                                record.getPfDeduction(),
                                record.getProfessionalTax(),
                                record.getIncomeTax(),
                                record.getOtherDeductions(),
                                record.getGrossSalary(),
                                record.getTotalDeductions(),
                                record.getNetSalary(),
                                record.getPayDate(),
                                record.getPayPeriod(),
                                record.getStatus());
        }

        public List<PayrollResponse> getAllPayroll() {
                return payrollRepository.findAll().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public byte[] generatePayslipPdf(UUID payrollId) {
                Payroll payroll = payrollRepository.findById(payrollId)
                                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        Document document = new Document();
                        PdfWriter.getInstance(document, out);
                        document.open();

                        document.add(new Paragraph("PAYSLIP"));
                        document.add(new Paragraph("Employee: " + payroll.getEmployee().getFirstName() + " "
                                        + payroll.getEmployee().getLastName()));
                        document.add(new Paragraph("Month: " + payroll.getMonth()));
                        document.add(new Paragraph("------------------------------------------------"));
                        document.add(new Paragraph("Base Salary: $" + payroll.getBaseSalary()));
                        document.add(new Paragraph("Overtime Pay: $" + payroll.getOvertimePay()));
                        document.add(new Paragraph("Deductions: $" + payroll.getDeductions()));
                        document.add(new Paragraph("------------------------------------------------"));
                        document.add(new Paragraph("Net Pay: $" + payroll.getNetPay()));

                        document.close();
                        return out.toByteArray();
                } catch (Exception e) {
                        throw new RuntimeException("Error generating PDF", e);
                }
        }

        public byte[] generateAnnualStatementPdf(UUID employeeId, int year) {
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Employee not found"));

                List<Payroll> payrolls = payrollRepository.findByEmployeeId(employeeId).stream()
                                .filter(p -> p.getMonth().contains(String.valueOf(year)))
                                .collect(Collectors.toList());

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        Document document = new Document();
                        PdfWriter.getInstance(document, out);
                        document.open();

                        document.add(new Paragraph("ANNUAL PAYROLL STATEMENT - " + year));
                        document.add(new Paragraph(
                                        "Employee: " + employee.getFirstName() + " " + employee.getLastName()));
                        document.add(new Paragraph("------------------------------------------------"));

                        double totalGross = 0;
                        double totalNet = 0;

                        for (Payroll p : payrolls) {
                                document.add(new Paragraph(
                                                p.getMonth() + ": Gross $" + (p.getBaseSalary() + p.getOvertimePay())
                                                                + ", Net $" + p.getNetPay()));
                                totalGross += (p.getBaseSalary() + p.getOvertimePay());
                                totalNet += p.getNetPay();
                        }

                        document.add(new Paragraph("------------------------------------------------"));
                        document.add(new Paragraph("Total Gross Pay: $" + String.format("%.2f", totalGross)));
                        document.add(new Paragraph("Total Net Pay: $" + String.format("%.2f", totalNet)));

                        document.close();
                        return out.toByteArray();
                } catch (Exception e) {
                        throw new RuntimeException("Error generating Annual Statement PDF", e);
                }
        }

        private PayrollResponse mapToResponse(Payroll payroll) {
                return new PayrollResponse(
                                payroll.getId(),
                                payroll.getEmployee().getId(),
                                payroll.getEmployee().getFirstName() + " " + payroll.getEmployee().getLastName(),
                                payroll.getMonth(),
                                payroll.getPayDate(),
                                payroll.getBaseSalary(),
                                payroll.getOvertimePay(),
                                payroll.getDeductions(),
                                payroll.getNetPay(),
                                payroll.getStatus(),
                                payroll.getRemarks());
        }
}

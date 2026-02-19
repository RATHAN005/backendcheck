package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.entity.Employee;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EmployeeRepository employeeRepository;

    public String generateEmployeeReport() {
        List<Employee> employees = employeeRepository.findAll();

        StringWriter stringWriter = new StringWriter();
        CsvWriter writer = new CsvWriter(stringWriter, new CsvWriterSettings());

        writer.writeHeaders("ID", "Name", "Email", "Department", "Job Title", "Hire Date", "Status");

        for (Employee emp : employees) {
            writer.writeRow(
                    emp.getId() != null ? emp.getId().toString() : "N/A",
                    emp.getFirstName() + " " + emp.getLastName(),
                    emp.getUser() != null ? emp.getUser().getEmail() : "N/A",
                    emp.getDepartment() != null ? emp.getDepartment().getName() : "N/A",
                    emp.getJobTitle() != null ? emp.getJobTitle() : "N/A",
                    emp.getHireDate() != null ? emp.getHireDate().toString() : "N/A",
                    emp.getEmploymentType() != null ? emp.getEmploymentType().toString() : "N/A");
        }

        writer.close();
        return stringWriter.toString();
    }
}

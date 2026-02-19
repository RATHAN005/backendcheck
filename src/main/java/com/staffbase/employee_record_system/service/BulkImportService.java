package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.EmployeeRequest;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BulkImportService {

    private final EmployeeService employeeService;
    private final AuditLogService auditLogService;

    public Map<String, Object> importEmployees(MultipartFile file, String adminEmail) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        List<String[]> rows;
        try {
            CsvParserSettings settings = new CsvParserSettings();
            settings.setHeaderExtractionEnabled(true);
            CsvParser parser = new CsvParser(settings);
            rows = parser.parseAll(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV", e);
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (String[] row : rows) {
            try {


                EmployeeRequest request = new EmployeeRequest(
                        row[0],
                        row[1],
                        row[0] + " " + row[1],
                        row[2],
                        row[3],
                        row[4],
                        null,
                        LocalDate.parse(row[5], DateTimeFormatter.ISO_LOCAL_DATE),
                        null, null, null, null, null, null, null, null, null, null, null
                );
                employeeService.createEmployee(request);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("Row error: " + e.getMessage());
            }
        }

        auditLogService.logAction("BULK_IMPORT", adminEmail,
                "Imported " + successCount + " employees, Failed: " + failCount);

        return Map.of(
                "successCount", successCount,
                "failCount", failCount,
                "errors", errors);
    }
}




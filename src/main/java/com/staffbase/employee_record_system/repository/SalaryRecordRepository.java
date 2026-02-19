package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, UUID> {
    List<SalaryRecord> findByEmployeeId(UUID employeeId);
}




package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    List<Payroll> findByEmployeeId(UUID employeeId);

    List<Payroll> findByMonth(String month);
}




package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    boolean existsByName(String name);
}




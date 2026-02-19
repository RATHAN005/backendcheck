package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByEmployeeId(UUID employeeId);
}




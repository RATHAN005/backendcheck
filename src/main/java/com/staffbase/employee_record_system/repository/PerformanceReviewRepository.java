package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, UUID> {
    List<PerformanceReview> findByEmployeeId(UUID employeeId);
}




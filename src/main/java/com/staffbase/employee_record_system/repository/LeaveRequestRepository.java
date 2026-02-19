package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByEmployeeId(UUID employeeId);

    long countByStatus(com.staffbase.employee_record_system.entity.LeaveStatus status);
}




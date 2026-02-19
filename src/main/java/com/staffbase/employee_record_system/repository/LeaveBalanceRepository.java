package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.LeaveBalance;
import com.staffbase.employee_record_system.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    List<LeaveBalance> findByEmployeeId(UUID employeeId);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveType(UUID employeeId, LeaveType leaveType);
}




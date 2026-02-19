package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Resignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResignationRepository extends JpaRepository<Resignation, UUID> {
    List<Resignation> findByEmployeeId(UUID employeeId);

    Optional<Resignation> findByEmployeeUserEmailAndStatus(String email,
            com.staffbase.employee_record_system.entity.ResignationStatus status);
}

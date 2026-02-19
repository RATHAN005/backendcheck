package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
        @org.springframework.data.jpa.repository.Query("SELECT e FROM Employee e WHERE e.user.email = :email")
        java.util.Optional<Employee> findByUserEmail(String email);

        @org.springframework.data.jpa.repository.Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
        java.util.Optional<Employee> findByUserId(UUID userId);

        @org.springframework.data.jpa.repository.Query("SELECT e FROM Employee e LEFT JOIN e.department d WHERE " +
                        "((LOWER(e.firstName) LIKE LOWER(:keyword) OR LOWER(e.lastName) LIKE LOWER(:keyword))) " +
                        "AND (:dept IS NULL OR LOWER(d.name) LIKE LOWER(:dept))")
        org.springframework.data.domain.Page<Employee> searchEmployees(
                        @org.springframework.data.repository.query.Param("keyword") String keyword,
                        @org.springframework.data.repository.query.Param("dept") String dept,
                        org.springframework.data.domain.Pageable pageable);
}

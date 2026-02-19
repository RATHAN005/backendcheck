package com.staffbase.employee_record_system.repository;



import com.staffbase.employee_record_system.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByEmployeeId(UUID employeeId);

    List<Attendance> findByDate(LocalDate date);

    long countByDate(LocalDate date);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.date BETWEEN :start AND :end")
    List<Attendance> findByEmployeeIdAndDateBetween(
            @org.springframework.data.repository.query.Param("employeeId") UUID employeeId,
            @org.springframework.data.repository.query.Param("start") LocalDate start,
            @org.springframework.data.repository.query.Param("end") LocalDate end);

    java.util.Optional<Attendance> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);
}




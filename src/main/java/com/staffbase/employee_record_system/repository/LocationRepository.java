package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
}

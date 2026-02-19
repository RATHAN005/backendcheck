package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
    Optional<SystemSettings> findBySettingKey(String settingKey);
}

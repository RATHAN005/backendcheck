package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.entity.Designation;
import com.staffbase.employee_record_system.entity.Location;
import com.staffbase.employee_record_system.entity.SystemSettings;
import com.staffbase.employee_record_system.repository.DesignationRepository;
import com.staffbase.employee_record_system.repository.LocationRepository;
import com.staffbase.employee_record_system.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final DesignationRepository designationRepository;
    private final LocationRepository locationRepository;
    private final SystemSettingsRepository systemSettingsRepository;
    private final AuditLogService auditLogService;

    // Designation Methods
    public List<Designation> getAllDesignations() {
        return designationRepository.findAll();
    }

    public Designation addDesignation(Designation designation, String adminEmail) {
        Designation saved = designationRepository.save(designation);
        auditLogService.logAction("CREATE_DESIGNATION", adminEmail, "Created designation: " + saved.getName());
        return saved;
    }

    public void deleteDesignation(UUID id, String adminEmail) {
        designationRepository.deleteById(id);
        auditLogService.logAction("DELETE_DESIGNATION", adminEmail, "Deleted designation with ID: " + id);
    }

    // Location Methods
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location addLocation(Location location, String adminEmail) {
        Location saved = locationRepository.save(location);
        auditLogService.logAction("CREATE_LOCATION", adminEmail, "Created location: " + saved.getName());
        return saved;
    }

    public void deleteLocation(UUID id, String adminEmail) {
        locationRepository.deleteById(id);
        auditLogService.logAction("DELETE_LOCATION", adminEmail, "Deleted location with ID: " + id);
    }

    // System Settings Methods
    public Map<String, String> getSystemSettings() {
        List<SystemSettings> settings = systemSettingsRepository.findAll();
        java.util.stream.Collector<SystemSettings, ?, Map<String, String>> collector = java.util.stream.Collectors
                .toMap(SystemSettings::getSettingKey, SystemSettings::getSettingValue);
        return settings.stream().collect(collector);
    }

    public void updateSystemSettings(Map<String, String> settings, String adminEmail) {
        settings.forEach((key, value) -> {
            SystemSettings setting = systemSettingsRepository.findBySettingKey(key)
                    .orElse(SystemSettings.builder().settingKey(key).build());
            setting.setSettingValue(value);
            systemSettingsRepository.save(setting);
        });
        auditLogService.logAction("UPDATE_SYSTEM_SETTINGS", adminEmail, "Updated system settings");
    }
}

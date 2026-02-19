package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.AuditLogResponse;
import com.staffbase.employee_record_system.entity.AuditLog;
import com.staffbase.employee_record_system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String performedBy, String entityName, String entityId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }

    public void logAction(String action, String performedBy, String details) {
        log(action, performedBy, null, null, details);
    }

    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getPerformedBy(),
                log.getEntityName(),
                log.getEntityId(),
                log.getDetails(),
                log.getTimestamp());
    }
}




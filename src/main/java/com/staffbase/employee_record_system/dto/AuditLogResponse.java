package com.staffbase.employee_record_system.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
                UUID id,
                String action,
                String performedBy,
                String entityName,
                String entityId,
                String details,
                LocalDateTime timestamp) {
}

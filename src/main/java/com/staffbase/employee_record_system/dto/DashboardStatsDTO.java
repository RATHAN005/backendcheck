package com.staffbase.employee_record_system.dto;

import java.util.Map;
import java.util.List;

public record DashboardStatsDTO(
        long totalEmployees,
        long totalDepartments,
        long totalDocuments,
        long pendingLeaves,
        long attendanceToday,
        Map<String, Long> departmentDistribution,
        List<Map<String, Object>> hiringTrends,
        List<Map<String, Object>> attendanceTrends,
        List<Map<String, Object>> payrollTrends,
        List<AuditLogResponse> recentActivities) {
}




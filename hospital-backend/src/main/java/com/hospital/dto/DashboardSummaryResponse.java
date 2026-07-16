package com.hospital.dto;

public record DashboardSummaryResponse(
        long totalPatients,
        long totalDepartments,
        long totalVisits,
        long todaysVisits) {
}

package com.hospital.dto;

public record DiscoveryDashboardSummaryResponse(
        long totalProjects,
        long activeSurveys,
        long completedSurveys,
        long totalQuestions,
        long answeredQuestions,
        long highRisks,
        long mediumRisks,
        long lowRisks) {
}

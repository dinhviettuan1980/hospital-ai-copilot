package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospital.dto.DiscoveryDashboardSummaryResponse;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.entity.DiscoveryRiskLevel;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

class DiscoveryDashboardServiceTest {

    private DiscoveryProjectRepository discoveryProjectRepository;
    private DiscoveryQuestionRepository discoveryQuestionRepository;
    private DiscoveryAnswerRepository discoveryAnswerRepository;
    private DiscoveryDashboardService discoveryDashboardService;

    @BeforeEach
    void setUp() {
        discoveryProjectRepository = mock(DiscoveryProjectRepository.class);
        discoveryQuestionRepository = mock(DiscoveryQuestionRepository.class);
        discoveryAnswerRepository = mock(DiscoveryAnswerRepository.class);
        discoveryDashboardService = new DiscoveryDashboardService(discoveryProjectRepository,
                discoveryQuestionRepository, discoveryAnswerRepository);
    }

    @Test
    void summaryAggregatesProjectQuestionAndRiskCounts() {
        when(discoveryProjectRepository.count()).thenReturn(5L);
        when(discoveryProjectRepository.count("status", DiscoveryProjectStatus.IN_PROGRESS)).thenReturn(2L);
        when(discoveryProjectRepository.count("status", DiscoveryProjectStatus.COMPLETED)).thenReturn(1L);
        when(discoveryQuestionRepository.count()).thenReturn(200L);
        when(discoveryAnswerRepository.count()).thenReturn(120L);
        when(discoveryAnswerRepository.countByRiskLevel(DiscoveryRiskLevel.HIGH)).thenReturn(3L);
        when(discoveryAnswerRepository.countByRiskLevel(DiscoveryRiskLevel.MEDIUM)).thenReturn(8L);
        when(discoveryAnswerRepository.countByRiskLevel(DiscoveryRiskLevel.LOW)).thenReturn(15L);

        DiscoveryDashboardSummaryResponse summary = discoveryDashboardService.summary();

        assertThat(summary.totalProjects()).isEqualTo(5L);
        assertThat(summary.activeSurveys()).isEqualTo(2L);
        assertThat(summary.completedSurveys()).isEqualTo(1L);
        assertThat(summary.totalQuestions()).isEqualTo(200L);
        assertThat(summary.answeredQuestions()).isEqualTo(120L);
        assertThat(summary.highRisks()).isEqualTo(3L);
        assertThat(summary.mediumRisks()).isEqualTo(8L);
        assertThat(summary.lowRisks()).isEqualTo(15L);
    }
}

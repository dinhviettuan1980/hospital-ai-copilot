package com.hospital.service;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryDashboardSummaryResponse;
import com.hospital.entity.DiscoveryProjectStatus;
import com.hospital.entity.DiscoveryRiskLevel;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

@ApplicationScoped
public class DiscoveryDashboardService {

    private final DiscoveryProjectRepository discoveryProjectRepository;
    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;

    public DiscoveryDashboardService(DiscoveryProjectRepository discoveryProjectRepository,
            DiscoveryQuestionRepository discoveryQuestionRepository,
            DiscoveryAnswerRepository discoveryAnswerRepository) {
        this.discoveryProjectRepository = discoveryProjectRepository;
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
    }

    public DiscoveryDashboardSummaryResponse summary() {
        return new DiscoveryDashboardSummaryResponse(
                discoveryProjectRepository.count(),
                discoveryProjectRepository.count("status", DiscoveryProjectStatus.IN_PROGRESS),
                discoveryProjectRepository.count("status", DiscoveryProjectStatus.COMPLETED),
                discoveryQuestionRepository.count(),
                discoveryAnswerRepository.count(),
                discoveryAnswerRepository.countByRiskLevel(DiscoveryRiskLevel.HIGH),
                discoveryAnswerRepository.countByRiskLevel(DiscoveryRiskLevel.MEDIUM),
                discoveryAnswerRepository.countByRiskLevel(DiscoveryRiskLevel.LOW));
    }
}

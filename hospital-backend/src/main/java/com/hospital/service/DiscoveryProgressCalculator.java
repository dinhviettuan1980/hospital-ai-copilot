package com.hospital.service;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryProgressResponse;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

/**
 * Shared by {@link DiscoveryProjectService}, {@link DiscoverySurveyService},
 * and {@link DiscoveryExportService} so "answered / total questions" is
 * computed exactly one way everywhere it's shown.
 */
@ApplicationScoped
public class DiscoveryProgressCalculator {

    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;

    public DiscoveryProgressCalculator(DiscoveryQuestionRepository discoveryQuestionRepository,
            DiscoveryAnswerRepository discoveryAnswerRepository) {
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
    }

    public DiscoveryProgressResponse forProject(UUID projectId) {
        long total = discoveryQuestionRepository.count();
        long answered = discoveryAnswerRepository.countByProject(projectId);
        return new DiscoveryProgressResponse((int) total, (int) answered, percent(answered, total));
    }

    public double percentOnly(UUID projectId) {
        return forProject(projectId).percent();
    }

    static double percent(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((part * 1000.0) / total) / 10.0;
    }
}

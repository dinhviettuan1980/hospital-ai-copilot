package com.hospital.dto;

import java.time.Instant;

import com.hospital.entity.DiscoveryRiskLevel;

public record DiscoveryAnswerResponse(
        String answerValue,
        String comment,
        DiscoveryRiskLevel riskLevel,
        Instant updatedAt) {
}

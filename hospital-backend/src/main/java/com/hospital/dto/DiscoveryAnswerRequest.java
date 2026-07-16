package com.hospital.dto;

import com.hospital.entity.DiscoveryRiskLevel;

import jakarta.validation.constraints.Size;

public record DiscoveryAnswerRequest(
        String answerValue,
        @Size(max = 1000) String comment,
        DiscoveryRiskLevel riskLevel) {
}

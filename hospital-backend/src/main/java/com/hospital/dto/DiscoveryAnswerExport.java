package com.hospital.dto;

import com.hospital.entity.DiscoveryRiskLevel;

public record DiscoveryAnswerExport(String value, String comment, DiscoveryRiskLevel riskLevel) {
}

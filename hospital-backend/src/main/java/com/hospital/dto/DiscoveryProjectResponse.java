package com.hospital.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.hospital.entity.DiscoveryProjectStatus;

public record DiscoveryProjectResponse(
        UUID id,
        String projectName,
        String hospitalName,
        String contactPerson,
        String contactEmail,
        String contactPhone,
        LocalDate surveyDate,
        DiscoveryProjectStatus status,
        String notes,
        double progressPercent,
        Instant createdAt,
        Instant updatedAt) {
}

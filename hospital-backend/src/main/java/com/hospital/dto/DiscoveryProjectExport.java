package com.hospital.dto;

import java.time.LocalDate;

import com.hospital.entity.DiscoveryProjectStatus;

public record DiscoveryProjectExport(
        String projectName,
        String hospitalName,
        String contactPerson,
        String contactEmail,
        String contactPhone,
        LocalDate surveyDate,
        DiscoveryProjectStatus status,
        String notes) {
}

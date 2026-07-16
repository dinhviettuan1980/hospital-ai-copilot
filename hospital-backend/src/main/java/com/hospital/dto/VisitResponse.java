package com.hospital.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.hospital.entity.VisitStatus;

public record VisitResponse(
        UUID id,
        PatientSummary patient,
        DepartmentSummary department,
        LocalDateTime visitDate,
        String reason,
        VisitStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt) {
}

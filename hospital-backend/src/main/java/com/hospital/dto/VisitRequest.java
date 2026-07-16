package com.hospital.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.hospital.entity.VisitStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VisitRequest(
        @NotNull UUID patientId,
        @NotNull UUID departmentId,
        @NotNull LocalDateTime visitDate,
        @NotNull @Size(max = 250) String reason,
        @NotNull VisitStatus status,
        @Size(max = 1000) String notes) {
}

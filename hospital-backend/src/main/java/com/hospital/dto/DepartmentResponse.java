package com.hospital.dto;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String name,
        String code,
        String description,
        Instant createdAt,
        Instant updatedAt) {
}

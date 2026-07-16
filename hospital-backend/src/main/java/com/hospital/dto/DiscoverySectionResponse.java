package com.hospital.dto;

import java.util.UUID;

public record DiscoverySectionResponse(
        UUID id,
        String code,
        String name,
        String description,
        int displayOrder) {
}

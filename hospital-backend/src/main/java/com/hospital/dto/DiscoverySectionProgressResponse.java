package com.hospital.dto;

import java.util.UUID;

public record DiscoverySectionProgressResponse(
        UUID id,
        String code,
        String name,
        String description,
        int displayOrder,
        int totalQuestions,
        int answeredQuestions,
        double percent) {
}

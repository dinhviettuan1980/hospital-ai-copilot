package com.hospital.dto;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeDocumentResponse(
        UUID id,
        String title,
        DocumentCategoryResponse category,
        String fileName,
        String contentType,
        long fileSize,
        Instant createdAt) {
}

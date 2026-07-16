package com.hospital.dto;

import java.time.Instant;
import java.util.UUID;

public record DiscoveryAttachmentResponse(
        UUID id,
        UUID questionId,
        String fileName,
        String contentType,
        long fileSize,
        Instant createdAt) {
}

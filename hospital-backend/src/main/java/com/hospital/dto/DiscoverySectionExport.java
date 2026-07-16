package com.hospital.dto;

import java.util.List;

public record DiscoverySectionExport(
        String code,
        String name,
        String description,
        int displayOrder,
        List<DiscoveryQuestionExport> questions) {
}

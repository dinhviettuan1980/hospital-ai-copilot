package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoverySectionProgressResponse;
import com.hospital.dto.DiscoverySectionResponse;
import com.hospital.entity.DiscoverySection;

@ApplicationScoped
public class DiscoverySectionMapper {

    public DiscoverySectionResponse toResponse(DiscoverySection section) {
        return new DiscoverySectionResponse(section.id, section.code, section.name, section.description,
                section.displayOrder);
    }

    public DiscoverySectionProgressResponse toProgressResponse(DiscoverySection section, int totalQuestions,
            int answeredQuestions) {
        double percent = totalQuestions == 0 ? 0.0 : Math.round((answeredQuestions * 1000.0) / totalQuestions) / 10.0;
        return new DiscoverySectionProgressResponse(section.id, section.code, section.name, section.description,
                section.displayOrder, totalQuestions, answeredQuestions, percent);
    }
}

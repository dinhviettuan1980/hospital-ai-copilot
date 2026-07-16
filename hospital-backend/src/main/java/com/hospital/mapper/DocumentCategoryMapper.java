package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DocumentCategoryResponse;
import com.hospital.entity.DocumentCategory;

@ApplicationScoped
public class DocumentCategoryMapper {

    public DocumentCategoryResponse toResponse(DocumentCategory category) {
        return new DocumentCategoryResponse(category.id, category.name);
    }
}

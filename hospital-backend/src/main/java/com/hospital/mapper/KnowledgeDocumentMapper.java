package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.KnowledgeDocumentResponse;
import com.hospital.entity.KnowledgeDocument;

@ApplicationScoped
public class KnowledgeDocumentMapper {

    private final DocumentCategoryMapper documentCategoryMapper;

    public KnowledgeDocumentMapper(DocumentCategoryMapper documentCategoryMapper) {
        this.documentCategoryMapper = documentCategoryMapper;
    }

    public KnowledgeDocumentResponse toResponse(KnowledgeDocument document) {
        return new KnowledgeDocumentResponse(
                document.id,
                document.title,
                documentCategoryMapper.toResponse(document.category),
                document.fileName,
                document.contentType,
                document.fileSize,
                document.createdAt);
    }
}

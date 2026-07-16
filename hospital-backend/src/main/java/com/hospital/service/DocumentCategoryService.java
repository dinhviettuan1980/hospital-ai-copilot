package com.hospital.service;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.DocumentCategoryRequest;
import com.hospital.dto.DocumentCategoryResponse;
import com.hospital.entity.DocumentCategory;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DocumentCategoryMapper;
import com.hospital.repository.DocumentCategoryRepository;

@ApplicationScoped
public class DocumentCategoryService {

    private final DocumentCategoryRepository documentCategoryRepository;
    private final DocumentCategoryMapper documentCategoryMapper;

    public DocumentCategoryService(DocumentCategoryRepository documentCategoryRepository,
            DocumentCategoryMapper documentCategoryMapper) {
        this.documentCategoryRepository = documentCategoryRepository;
        this.documentCategoryMapper = documentCategoryMapper;
    }

    public List<DocumentCategoryResponse> list() {
        return documentCategoryRepository.listAllSorted().stream()
                .map(documentCategoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public DocumentCategoryResponse create(DocumentCategoryRequest request) {
        if (documentCategoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("A category named '" + request.name() + "' already exists");
        }
        DocumentCategory category = new DocumentCategory();
        category.name = request.name();
        documentCategoryRepository.persist(category);
        return documentCategoryMapper.toResponse(category);
    }

    DocumentCategory findOrThrow(UUID id) {
        return documentCategoryRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Document category", id));
    }
}

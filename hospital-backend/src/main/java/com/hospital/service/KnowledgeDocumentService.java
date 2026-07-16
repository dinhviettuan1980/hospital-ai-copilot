package com.hospital.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.KnowledgeDocumentResponse;
import com.hospital.dto.PageResponse;
import com.hospital.entity.DocumentCategory;
import com.hospital.entity.KnowledgeDocument;
import com.hospital.exception.InvalidFileException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.KnowledgeDocumentMapper;
import com.hospital.repository.KnowledgeDocumentRepository;
import com.hospital.storage.LocalFileStorage;

@ApplicationScoped
public class KnowledgeDocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final DocumentCategoryService documentCategoryService;
    private final LocalFileStorage storage;

    public KnowledgeDocumentService(KnowledgeDocumentRepository knowledgeDocumentRepository,
            KnowledgeDocumentMapper knowledgeDocumentMapper, DocumentCategoryService documentCategoryService,
            @ConfigProperty(name = "hospital.knowledge.storage-path") String storagePath) {
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.documentCategoryService = documentCategoryService;
        this.storage = new LocalFileStorage(storagePath);
    }

    public PageResponse<KnowledgeDocumentResponse> list(String title, UUID categoryId, int page, int size) {
        var query = knowledgeDocumentRepository.search(title, categoryId, Page.of(page, size),
                Sort.by("title"));
        List<KnowledgeDocumentResponse> content = query.list().stream()
                .map(knowledgeDocumentMapper::toResponse)
                .toList();
        return PageResponse.of(content, page, size, query.count());
    }

    @Transactional
    public KnowledgeDocumentResponse upload(String title, UUID categoryId, FileUpload file) {
        if (title == null || title.isBlank()) {
            throw new InvalidFileException("Title is required");
        }
        if (file == null || file.fileName() == null || file.fileName().isBlank()) {
            throw new InvalidFileException("A file is required");
        }
        String extension = LocalFileStorage.extensionOf(file.fileName());
        if (!ALLOWED_EXTENSIONS.contains(extension) && !ALLOWED_CONTENT_TYPES.contains(file.contentType())) {
            throw new InvalidFileException("Only PDF and DOCX files are supported");
        }

        DocumentCategory category = documentCategoryService.findOrThrow(categoryId);
        String storedFileName = storage.store(file.uploadedFile(), file.fileName());

        KnowledgeDocument document = new KnowledgeDocument();
        document.title = title.trim();
        document.category = category;
        document.fileName = file.fileName();
        document.contentType = file.contentType() != null ? file.contentType() : "application/octet-stream";
        document.fileSize = file.size();
        document.storagePath = storedFileName;
        knowledgeDocumentRepository.persist(document);

        return knowledgeDocumentMapper.toResponse(document);
    }

    public KnowledgeDocument findOrThrow(UUID id) {
        return knowledgeDocumentRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Document", id));
    }

    public java.nio.file.Path resolveStoredFile(KnowledgeDocument document) {
        return storage.resolve(document.storagePath);
    }

    @Transactional
    public void delete(UUID id) {
        KnowledgeDocument document = findOrThrow(id);
        knowledgeDocumentRepository.delete(document);
        storage.deleteQuietly(document.storagePath);
    }
}

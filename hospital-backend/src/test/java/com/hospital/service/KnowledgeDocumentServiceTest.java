package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.hospital.dto.KnowledgeDocumentResponse;
import com.hospital.entity.DocumentCategory;
import com.hospital.entity.KnowledgeDocument;
import com.hospital.exception.InvalidFileException;
import com.hospital.mapper.DocumentCategoryMapper;
import com.hospital.mapper.KnowledgeDocumentMapper;
import com.hospital.repository.DocumentCategoryRepository;
import com.hospital.repository.KnowledgeDocumentRepository;

class KnowledgeDocumentServiceTest {

    @TempDir
    Path storageDir;

    @TempDir
    Path uploadSourceDir;

    private KnowledgeDocumentRepository knowledgeDocumentRepository;
    private DocumentCategoryRepository documentCategoryRepository;
    private KnowledgeDocumentService knowledgeDocumentService;
    private DocumentCategory category;

    @BeforeEach
    void setUp() {
        knowledgeDocumentRepository = mock(KnowledgeDocumentRepository.class);
        documentCategoryRepository = mock(DocumentCategoryRepository.class);
        DocumentCategoryService documentCategoryService = new DocumentCategoryService(documentCategoryRepository,
                new DocumentCategoryMapper());
        KnowledgeDocumentMapper mapper = new KnowledgeDocumentMapper(new DocumentCategoryMapper());
        knowledgeDocumentService = new KnowledgeDocumentService(knowledgeDocumentRepository, mapper,
                documentCategoryService, storageDir.toString());

        category = new DocumentCategory();
        category.id = UUID.randomUUID();
        category.name = "Policy";
        when(documentCategoryRepository.findByIdOptional(category.id)).thenReturn(Optional.of(category));
    }

    private FileUpload fakeUpload(String fileName, String contentType, byte[] bytes) throws IOException {
        Path sourceFile = uploadSourceDir.resolve(UUID.randomUUID() + "-src");
        Files.write(sourceFile, bytes);

        FileUpload upload = mock(FileUpload.class);
        when(upload.fileName()).thenReturn(fileName);
        when(upload.contentType()).thenReturn(contentType);
        when(upload.size()).thenReturn((long) bytes.length);
        when(upload.uploadedFile()).thenReturn(sourceFile);
        return upload;
    }

    @Test
    void uploadStoresFileAndPersistsMetadata() throws IOException {
        FileUpload upload = fakeUpload("policy.pdf", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8));

        KnowledgeDocumentResponse response = knowledgeDocumentService.upload("ICU Policy", category.id, upload);

        assertThat(response.title()).isEqualTo("ICU Policy");
        verify(knowledgeDocumentRepository).persist(org.mockito.ArgumentMatchers.any(KnowledgeDocument.class));
        try (var files = Files.list(storageDir)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    void uploadRejectsUnsupportedFileType() throws IOException {
        FileUpload upload = fakeUpload("virus.exe", "application/octet-stream", "x".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> knowledgeDocumentService.upload("Bad file", category.id, upload))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void uploadRejectsBlankTitle() throws IOException {
        FileUpload upload = fakeUpload("policy.pdf", "application/pdf", "hello".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> knowledgeDocumentService.upload("  ", category.id, upload))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void acceptsDocxByExtensionEvenWithGenericContentType() throws IOException {
        FileUpload upload = fakeUpload("guideline.docx", "application/octet-stream",
                "hello".getBytes(StandardCharsets.UTF_8));

        KnowledgeDocumentResponse response = knowledgeDocumentService.upload("Guideline", category.id, upload);

        assertThat(response.fileName()).isEqualTo("guideline.docx");
    }
}

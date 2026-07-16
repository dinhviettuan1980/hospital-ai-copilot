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

import com.hospital.dto.DiscoveryAttachmentResponse;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.exception.InvalidFileException;
import com.hospital.mapper.DiscoveryAttachmentMapper;
import com.hospital.repository.DiscoveryAttachmentRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;

class DiscoveryAttachmentServiceTest {

    @TempDir
    Path storageDir;

    @TempDir
    Path uploadSourceDir;

    private DiscoveryAttachmentRepository discoveryAttachmentRepository;
    private DiscoveryQuestionRepository discoveryQuestionRepository;
    private DiscoveryProjectRepository discoveryProjectRepository;
    private DiscoveryAttachmentService discoveryAttachmentService;
    private DiscoveryProject project;

    @BeforeEach
    void setUp() {
        discoveryAttachmentRepository = mock(DiscoveryAttachmentRepository.class);
        discoveryQuestionRepository = mock(DiscoveryQuestionRepository.class);
        discoveryProjectRepository = mock(DiscoveryProjectRepository.class);

        discoveryAttachmentService = new DiscoveryAttachmentService(discoveryAttachmentRepository,
                new DiscoveryAttachmentMapper(), discoveryQuestionRepository, discoveryProjectRepository,
                storageDir.toString());

        project = new DiscoveryProject();
        project.id = UUID.randomUUID();
        when(discoveryProjectRepository.findByIdOptional(project.id)).thenReturn(Optional.of(project));
    }

    private FileUpload fakeUpload(String fileName, byte[] bytes) throws IOException {
        Path sourceFile = uploadSourceDir.resolve(UUID.randomUUID() + "-src");
        Files.write(sourceFile, bytes);

        FileUpload upload = mock(FileUpload.class);
        when(upload.fileName()).thenReturn(fileName);
        when(upload.contentType()).thenReturn("application/octet-stream");
        when(upload.size()).thenReturn((long) bytes.length);
        when(upload.uploadedFile()).thenReturn(sourceFile);
        return upload;
    }

    @Test
    void uploadWithoutQuestionStoresFileAndPersistsMetadata() throws IOException {
        FileUpload upload = fakeUpload("evidence.pdf", "hello".getBytes(StandardCharsets.UTF_8));

        DiscoveryAttachmentResponse response = discoveryAttachmentService.upload(project.id, null, upload);

        assertThat(response.fileName()).isEqualTo("evidence.pdf");
        verify(discoveryAttachmentRepository).persist(org.mockito.ArgumentMatchers.any(com.hospital.entity.DiscoveryAttachment.class));
        try (var files = Files.list(storageDir)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    void uploadWithQuestionLinksToThatQuestion() throws IOException {
        DiscoveryQuestion question = new DiscoveryQuestion();
        question.id = UUID.randomUUID();
        when(discoveryQuestionRepository.findByIdOptional(question.id)).thenReturn(Optional.of(question));
        FileUpload upload = fakeUpload("scan.png", "bytes".getBytes(StandardCharsets.UTF_8));

        DiscoveryAttachmentResponse response = discoveryAttachmentService.upload(project.id, question.id, upload);

        assertThat(response.questionId()).isEqualTo(question.id);
    }

    @Test
    void uploadRejectsUnsupportedFileType() throws IOException {
        FileUpload upload = fakeUpload("malware.exe", "x".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> discoveryAttachmentService.upload(project.id, null, upload))
                .isInstanceOf(InvalidFileException.class);
    }
}

package com.hospital.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.DiscoveryAttachmentResponse;
import com.hospital.entity.DiscoveryAttachment;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.exception.InvalidFileException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DiscoveryAttachmentMapper;
import com.hospital.repository.DiscoveryAttachmentRepository;
import com.hospital.repository.DiscoveryProjectRepository;
import com.hospital.repository.DiscoveryQuestionRepository;
import com.hospital.storage.LocalFileStorage;

/**
 * Depends on {@link DiscoveryProjectRepository} directly (not
 * {@link DiscoveryProjectService}) so that service can depend on this one
 * for cascading project deletes without a circular CDI dependency.
 */
@ApplicationScoped
public class DiscoveryAttachmentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "xlsx", "png", "jpg", "jpeg");

    private final DiscoveryAttachmentRepository discoveryAttachmentRepository;
    private final DiscoveryAttachmentMapper discoveryAttachmentMapper;
    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryProjectRepository discoveryProjectRepository;
    private final LocalFileStorage storage;

    public DiscoveryAttachmentService(DiscoveryAttachmentRepository discoveryAttachmentRepository,
            DiscoveryAttachmentMapper discoveryAttachmentMapper, DiscoveryQuestionRepository discoveryQuestionRepository,
            DiscoveryProjectRepository discoveryProjectRepository,
            @ConfigProperty(name = "hospital.discovery.storage-path") String storagePath) {
        this.discoveryAttachmentRepository = discoveryAttachmentRepository;
        this.discoveryAttachmentMapper = discoveryAttachmentMapper;
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryProjectRepository = discoveryProjectRepository;
        this.storage = new LocalFileStorage(storagePath);
    }

    public List<DiscoveryAttachmentResponse> listByProject(UUID projectId) {
        findProjectOrThrow(projectId);
        return discoveryAttachmentRepository.listByProject(projectId).stream()
                .map(discoveryAttachmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public DiscoveryAttachmentResponse upload(UUID projectId, UUID questionId, FileUpload file) {
        DiscoveryProject project = findProjectOrThrow(projectId);
        if (file == null || file.fileName() == null || file.fileName().isBlank()) {
            throw new InvalidFileException("A file is required");
        }
        String extension = LocalFileStorage.extensionOf(file.fileName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("Only PDF, DOCX, XLSX, PNG, and JPG files are supported");
        }

        DiscoveryQuestion question = questionId == null ? null
                : discoveryQuestionRepository.findByIdOptional(questionId)
                        .orElseThrow(() -> ResourceNotFoundException.of("Discovery question", questionId));

        String storedFileName = storage.store(file.uploadedFile(), file.fileName());

        DiscoveryAttachment attachment = new DiscoveryAttachment();
        attachment.project = project;
        attachment.question = question;
        attachment.fileName = file.fileName();
        attachment.contentType = file.contentType() != null ? file.contentType() : "application/octet-stream";
        attachment.fileSize = file.size();
        attachment.storagePath = storedFileName;
        discoveryAttachmentRepository.persist(attachment);

        return discoveryAttachmentMapper.toResponse(attachment);
    }

    public DiscoveryAttachment findOrThrow(UUID id) {
        return discoveryAttachmentRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Attachment", id));
    }

    public Path resolveStoredFile(DiscoveryAttachment attachment) {
        return storage.resolve(attachment.storagePath);
    }

    @Transactional
    public void delete(UUID id) {
        DiscoveryAttachment attachment = findOrThrow(id);
        discoveryAttachmentRepository.delete(attachment);
        storage.deleteQuietly(attachment.storagePath);
    }

    /** Deletes every attachment (DB row and file) for a project. Used when the project itself is deleted. */
    @Transactional
    public void deleteAllForProject(UUID projectId) {
        List<DiscoveryAttachment> attachments = discoveryAttachmentRepository.listByProject(projectId);
        for (DiscoveryAttachment attachment : attachments) {
            storage.deleteQuietly(attachment.storagePath);
        }
        discoveryAttachmentRepository.delete("project.id", projectId);
    }

    private DiscoveryProject findProjectOrThrow(UUID projectId) {
        return discoveryProjectRepository.findByIdOptional(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Discovery project", projectId));
    }
}

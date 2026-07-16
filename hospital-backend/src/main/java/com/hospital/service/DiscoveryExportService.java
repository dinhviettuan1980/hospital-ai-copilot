package com.hospital.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryAnswerExport;
import com.hospital.dto.DiscoveryAttachmentExport;
import com.hospital.dto.DiscoveryQuestionExport;
import com.hospital.dto.DiscoverySectionExport;
import com.hospital.dto.DiscoverySurveyExport;
import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryAttachment;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.entity.DiscoverySection;
import com.hospital.mapper.DiscoveryAnswerMapper;
import com.hospital.mapper.DiscoveryAttachmentMapper;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.mapper.DiscoveryQuestionMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryAttachmentRepository;
import com.hospital.repository.DiscoveryQuestionRepository;
import com.hospital.repository.DiscoverySectionRepository;

/**
 * Builds the "clean and extensible" survey export described in the spec —
 * a complete, self-contained snapshot of one project's answers, designed to
 * double as the import format and to be consumable by future AI modules.
 *
 * <p>Answers and attachments are loaded once per project (not once per
 * question) and matched in memory — with ~200 questions and a remote
 * database, one query per question turned this into a multi-second N+1
 * request; two bulk queries do not.
 */
@ApplicationScoped
public class DiscoveryExportService {

    private final DiscoveryProjectService discoveryProjectService;
    private final DiscoverySectionRepository discoverySectionRepository;
    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;
    private final DiscoveryAttachmentRepository discoveryAttachmentRepository;
    private final DiscoveryProjectMapper discoveryProjectMapper;
    private final DiscoveryQuestionMapper discoveryQuestionMapper;
    private final DiscoveryAnswerMapper discoveryAnswerMapper;
    private final DiscoveryAttachmentMapper discoveryAttachmentMapper;
    private final DiscoveryProgressCalculator progressCalculator;

    public DiscoveryExportService(DiscoveryProjectService discoveryProjectService,
            DiscoverySectionRepository discoverySectionRepository,
            DiscoveryQuestionRepository discoveryQuestionRepository,
            DiscoveryAnswerRepository discoveryAnswerRepository,
            DiscoveryAttachmentRepository discoveryAttachmentRepository,
            DiscoveryProjectMapper discoveryProjectMapper, DiscoveryQuestionMapper discoveryQuestionMapper,
            DiscoveryAnswerMapper discoveryAnswerMapper, DiscoveryAttachmentMapper discoveryAttachmentMapper,
            DiscoveryProgressCalculator progressCalculator) {
        this.discoveryProjectService = discoveryProjectService;
        this.discoverySectionRepository = discoverySectionRepository;
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
        this.discoveryAttachmentRepository = discoveryAttachmentRepository;
        this.discoveryProjectMapper = discoveryProjectMapper;
        this.discoveryQuestionMapper = discoveryQuestionMapper;
        this.discoveryAnswerMapper = discoveryAnswerMapper;
        this.discoveryAttachmentMapper = discoveryAttachmentMapper;
        this.progressCalculator = progressCalculator;
    }

    public DiscoverySurveyExport export(UUID projectId) {
        DiscoveryProject project = discoveryProjectService.findOrThrow(projectId);

        Map<UUID, DiscoveryAnswer> answersByQuestionId = discoveryAnswerRepository.listByProject(projectId).stream()
                .collect(Collectors.toMap(a -> a.question.id, a -> a));
        Map<UUID, List<DiscoveryAttachment>> attachmentsByQuestionId = discoveryAttachmentRepository
                .listByProject(projectId).stream()
                .filter(a -> a.question != null)
                .collect(Collectors.groupingBy(a -> a.question.id));

        List<DiscoverySectionExport> sections = discoverySectionRepository.listAllOrdered().stream()
                .map(section -> exportSection(section, answersByQuestionId, attachmentsByQuestionId))
                .toList();

        return new DiscoverySurveyExport(
                DiscoverySurveyExport.CURRENT_VERSION,
                Instant.now(),
                discoveryProjectMapper.toExport(project),
                progressCalculator.forProject(projectId),
                sections);
    }

    private DiscoverySectionExport exportSection(DiscoverySection section,
            Map<UUID, DiscoveryAnswer> answersByQuestionId, Map<UUID, List<DiscoveryAttachment>> attachmentsByQuestionId) {
        List<DiscoveryQuestionExport> questions = discoveryQuestionRepository.listBySection(section.id).stream()
                .map(question -> exportQuestion(question, answersByQuestionId, attachmentsByQuestionId))
                .toList();
        return new DiscoverySectionExport(section.code, section.name, section.description, section.displayOrder,
                questions);
    }

    private DiscoveryQuestionExport exportQuestion(DiscoveryQuestion question,
            Map<UUID, DiscoveryAnswer> answersByQuestionId, Map<UUID, List<DiscoveryAttachment>> attachmentsByQuestionId) {
        DiscoveryAnswerExport answer = discoveryAnswerMapper.toExport(answersByQuestionId.get(question.id));
        List<DiscoveryAttachmentExport> attachments = attachmentsByQuestionId
                .getOrDefault(question.id, List.of()).stream()
                .map(discoveryAttachmentMapper::toExport)
                .toList();
        return discoveryQuestionMapper.toExport(question, answer, attachments);
    }
}

package com.hospital.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.DiscoveryAnswerRequest;
import com.hospital.dto.DiscoveryAnswerResponse;
import com.hospital.dto.DiscoverySectionProgressResponse;
import com.hospital.dto.DiscoveryQuestionResponse;
import com.hospital.dto.DiscoveryQuestionWithAnswerResponse;
import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryProject;
import com.hospital.entity.DiscoveryQuestion;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DiscoveryAnswerMapper;
import com.hospital.mapper.DiscoveryAttachmentMapper;
import com.hospital.mapper.DiscoverySectionMapper;
import com.hospital.mapper.DiscoveryQuestionMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryAttachmentRepository;
import com.hospital.repository.DiscoveryQuestionRepository;
import com.hospital.repository.DiscoverySectionRepository;

/**
 * Powers the Notion-like survey workspace: sections with per-section
 * progress for the sidebar, questions-with-current-answer for the question
 * list, and the answer upsert that backs auto-save.
 *
 * <p>{@code listSectionsWithProgress} and {@code listQuestions} each load
 * every answer/attachment for the project in one bulk query and match them
 * in memory, rather than one query per question — with ~200 questions
 * against a remote database, per-question queries turned a single page
 * load into hundreds of sequential round-trips.
 */
@ApplicationScoped
public class DiscoverySurveyService {

    private final DiscoverySectionRepository discoverySectionRepository;
    private final DiscoveryQuestionRepository discoveryQuestionRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;
    private final DiscoveryAttachmentRepository discoveryAttachmentRepository;
    private final DiscoverySectionMapper discoverySectionMapper;
    private final DiscoveryQuestionMapper discoveryQuestionMapper;
    private final DiscoveryAnswerMapper discoveryAnswerMapper;
    private final DiscoveryAttachmentMapper discoveryAttachmentMapper;
    private final DiscoveryProjectService discoveryProjectService;

    public DiscoverySurveyService(DiscoverySectionRepository discoverySectionRepository,
            DiscoveryQuestionRepository discoveryQuestionRepository,
            DiscoveryAnswerRepository discoveryAnswerRepository,
            DiscoveryAttachmentRepository discoveryAttachmentRepository,
            DiscoverySectionMapper discoverySectionMapper, DiscoveryQuestionMapper discoveryQuestionMapper,
            DiscoveryAnswerMapper discoveryAnswerMapper, DiscoveryAttachmentMapper discoveryAttachmentMapper,
            DiscoveryProjectService discoveryProjectService) {
        this.discoverySectionRepository = discoverySectionRepository;
        this.discoveryQuestionRepository = discoveryQuestionRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
        this.discoveryAttachmentRepository = discoveryAttachmentRepository;
        this.discoverySectionMapper = discoverySectionMapper;
        this.discoveryQuestionMapper = discoveryQuestionMapper;
        this.discoveryAnswerMapper = discoveryAnswerMapper;
        this.discoveryAttachmentMapper = discoveryAttachmentMapper;
        this.discoveryProjectService = discoveryProjectService;
    }

    public List<DiscoverySectionProgressResponse> listSectionsWithProgress(UUID projectId) {
        discoveryProjectService.findOrThrow(projectId);

        Set<UUID> answeredQuestionIds = discoveryAnswerRepository.listByProject(projectId).stream()
                .map(a -> a.question.id)
                .collect(Collectors.toCollection(HashSet::new));

        return discoverySectionRepository.listAllOrdered().stream()
                .map(section -> {
                    List<DiscoveryQuestion> questions = discoveryQuestionRepository.listBySection(section.id);
                    long answered = questions.stream().filter(q -> answeredQuestionIds.contains(q.id)).count();
                    return discoverySectionMapper.toProgressResponse(section, questions.size(), (int) answered);
                })
                .toList();
    }

    public List<DiscoveryQuestionWithAnswerResponse> listQuestions(UUID projectId, UUID sectionId) {
        discoveryProjectService.findOrThrow(projectId);

        Map<UUID, DiscoveryAnswer> answersByQuestionId = discoveryAnswerRepository.listByProject(projectId).stream()
                .collect(Collectors.toMap(a -> a.question.id, a -> a));
        Map<UUID, List<com.hospital.entity.DiscoveryAttachment>> attachmentsByQuestionId = discoveryAttachmentRepository
                .listByProject(projectId).stream()
                .filter(a -> a.question != null)
                .collect(Collectors.groupingBy(a -> a.question.id));

        return discoveryQuestionRepository.listBySection(sectionId).stream()
                .map(question -> {
                    DiscoveryAnswerResponse answer = discoveryAnswerMapper.toResponse(answersByQuestionId.get(question.id));
                    var attachments = attachmentsByQuestionId.getOrDefault(question.id, List.of()).stream()
                            .map(discoveryAttachmentMapper::toResponse)
                            .toList();
                    return discoveryQuestionMapper.toResponseWithAnswer(question, answer, attachments);
                })
                .toList();
    }

    public List<DiscoveryQuestionResponse> searchQuestions(String query, UUID sectionId) {
        return discoveryQuestionRepository.search(query, sectionId).stream()
                .map(discoveryQuestionMapper::toResponse)
                .toList();
    }

    @Transactional
    public DiscoveryAnswerResponse saveAnswer(UUID projectId, UUID questionId, DiscoveryAnswerRequest request) {
        DiscoveryProject project = discoveryProjectService.findOrThrow(projectId);
        DiscoveryQuestion question = discoveryQuestionRepository.findByIdOptional(questionId)
                .orElseThrow(() -> ResourceNotFoundException.of("Discovery question", questionId));

        DiscoveryAnswer answer = discoveryAnswerRepository.findByProjectAndQuestion(projectId, questionId)
                .orElseGet(() -> {
                    DiscoveryAnswer created = new DiscoveryAnswer();
                    created.project = project;
                    created.question = question;
                    return created;
                });

        answer.answerValue = request.answerValue();
        answer.comment = request.comment();
        answer.riskLevel = request.riskLevel();

        if (answer.id == null) {
            discoveryAnswerRepository.persist(answer);
        }

        return discoveryAnswerMapper.toResponse(answer);
    }
}

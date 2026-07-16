package com.hospital.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.DiscoveryProjectRequest;
import com.hospital.dto.DiscoveryProjectResponse;
import com.hospital.dto.PageResponse;
import com.hospital.entity.DiscoveryProject;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DiscoveryProjectMapper;
import com.hospital.repository.DiscoveryAnswerRepository;
import com.hospital.repository.DiscoveryProjectRepository;

@ApplicationScoped
public class DiscoveryProjectService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "projectName", "hospitalName", "surveyDate", "status", "createdAt", "updatedAt");
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final DiscoveryProjectRepository discoveryProjectRepository;
    private final DiscoveryAnswerRepository discoveryAnswerRepository;
    private final DiscoveryAttachmentService discoveryAttachmentService;
    private final DiscoveryProjectMapper discoveryProjectMapper;
    private final DiscoveryProgressCalculator progressCalculator;

    public DiscoveryProjectService(DiscoveryProjectRepository discoveryProjectRepository,
            DiscoveryAnswerRepository discoveryAnswerRepository, DiscoveryAttachmentService discoveryAttachmentService,
            DiscoveryProjectMapper discoveryProjectMapper, DiscoveryProgressCalculator progressCalculator) {
        this.discoveryProjectRepository = discoveryProjectRepository;
        this.discoveryAnswerRepository = discoveryAnswerRepository;
        this.discoveryAttachmentService = discoveryAttachmentService;
        this.discoveryProjectMapper = discoveryProjectMapper;
        this.progressCalculator = progressCalculator;
    }

    public PageResponse<DiscoveryProjectResponse> list(String query, int page, int size, String sortBy,
            String sortDir) {
        Sort sort = SortSupport.build(sortBy, sortDir, SORTABLE_FIELDS, DEFAULT_SORT_FIELD);
        var panacheQuery = discoveryProjectRepository.search(query, Page.of(page, size), sort);

        List<DiscoveryProjectResponse> content = panacheQuery.list().stream()
                .map(project -> discoveryProjectMapper.toResponse(project, progressCalculator.percentOnly(project.id)))
                .toList();

        return PageResponse.of(content, page, size, panacheQuery.count());
    }

    public DiscoveryProjectResponse get(UUID id) {
        DiscoveryProject project = findOrThrow(id);
        return discoveryProjectMapper.toResponse(project, progressCalculator.percentOnly(id));
    }

    @Transactional
    public DiscoveryProjectResponse create(DiscoveryProjectRequest request) {
        DiscoveryProject project = new DiscoveryProject();
        applyRequest(project, request);
        discoveryProjectRepository.persist(project);
        return discoveryProjectMapper.toResponse(project, 0.0);
    }

    @Transactional
    public DiscoveryProjectResponse update(UUID id, DiscoveryProjectRequest request) {
        DiscoveryProject project = findOrThrow(id);
        applyRequest(project, request);
        return discoveryProjectMapper.toResponse(project, progressCalculator.percentOnly(id));
    }

    @Transactional
    public void delete(UUID id) {
        DiscoveryProject project = findOrThrow(id);
        // Answers and attachments reference the project via a FK with no cascade at
        // the DB level, so both must be cleared before the project row itself.
        discoveryAnswerRepository.delete("project.id", id);
        discoveryAttachmentService.deleteAllForProject(id);
        discoveryProjectRepository.delete(project);
    }

    DiscoveryProject findOrThrow(UUID id) {
        return discoveryProjectRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Discovery project", id));
    }

    private void applyRequest(DiscoveryProject project, DiscoveryProjectRequest request) {
        project.projectName = request.projectName();
        project.hospitalName = request.hospitalName();
        project.contactPerson = request.contactPerson();
        project.contactEmail = request.contactEmail();
        project.contactPhone = request.contactPhone();
        project.surveyDate = request.surveyDate();
        project.status = request.status();
        project.notes = request.notes();
    }
}

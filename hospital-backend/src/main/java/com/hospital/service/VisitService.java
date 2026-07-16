package com.hospital.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.PageResponse;
import com.hospital.dto.VisitRequest;
import com.hospital.dto.VisitResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Patient;
import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.VisitMapper;
import com.hospital.repository.VisitRepository;

@ApplicationScoped
public class VisitService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("visitDate", "status", "createdAt", "updatedAt");
    private static final String DEFAULT_SORT_FIELD = "visitDate";

    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;
    private final PatientService patientService;
    private final DepartmentService departmentService;

    public VisitService(VisitRepository visitRepository, VisitMapper visitMapper,
            PatientService patientService, DepartmentService departmentService) {
        this.visitRepository = visitRepository;
        this.visitMapper = visitMapper;
        this.patientService = patientService;
        this.departmentService = departmentService;
    }

    public PageResponse<VisitResponse> list(String query, UUID departmentId, UUID patientId, VisitStatus status,
            int page, int size, String sortBy, String sortDir) {
        Sort sort = SortSupport.build(sortBy, sortDir, SORTABLE_FIELDS, DEFAULT_SORT_FIELD);
        var panacheQuery = visitRepository.search(query, departmentId, patientId, status, Page.of(page, size), sort);

        List<VisitResponse> content = panacheQuery.list().stream()
                .map(visitMapper::toResponse)
                .toList();

        return PageResponse.of(content, page, size, panacheQuery.count());
    }

    public VisitResponse get(UUID id) {
        return visitMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public VisitResponse create(VisitRequest request) {
        Visit visit = new Visit();
        applyRequest(visit, request);
        visitRepository.persist(visit);
        return visitMapper.toResponse(visit);
    }

    @Transactional
    public VisitResponse update(UUID id, VisitRequest request) {
        Visit visit = findOrThrow(id);
        applyRequest(visit, request);
        return visitMapper.toResponse(visit);
    }

    @Transactional
    public void delete(UUID id) {
        Visit visit = findOrThrow(id);
        visitRepository.delete(visit);
    }

    private Visit findOrThrow(UUID id) {
        return visitRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Visit", id));
    }

    private void applyRequest(Visit visit, VisitRequest request) {
        Patient patient = patientService.findOrThrow(request.patientId());
        Department department = departmentService.findOrThrow(request.departmentId());

        visit.patient = patient;
        visit.department = department;
        visit.visitDate = request.visitDate();
        visit.reason = request.reason();
        visit.status = request.status();
        visit.notes = request.notes();
    }
}

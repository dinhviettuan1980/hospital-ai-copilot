package com.hospital.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.PageResponse;
import com.hospital.dto.PatientRequest;
import com.hospital.dto.PatientResponse;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.PatientMapper;
import com.hospital.repository.PatientRepository;

@ApplicationScoped
public class PatientService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "firstName", "lastName", "dateOfBirth", "createdAt", "updatedAt");
    private static final String DEFAULT_SORT_FIELD = "lastName";

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public PageResponse<PatientResponse> list(String query, int page, int size, String sortBy, String sortDir) {
        Sort sort = SortSupport.build(sortBy, sortDir, SORTABLE_FIELDS, DEFAULT_SORT_FIELD);
        var panacheQuery = patientRepository.search(query, Page.of(page, size), sort);

        List<PatientResponse> content = panacheQuery.list().stream()
                .map(patientMapper::toResponse)
                .toList();

        return PageResponse.of(content, page, size, panacheQuery.count());
    }

    public PatientResponse get(UUID id) {
        return patientMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        Patient patient = new Patient();
        applyRequest(patient, request);
        patientRepository.persist(patient);
        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponse update(UUID id, PatientRequest request) {
        Patient patient = findOrThrow(id);
        applyRequest(patient, request);
        return patientMapper.toResponse(patient);
    }

    @Transactional
    public void delete(UUID id) {
        Patient patient = findOrThrow(id);
        patientRepository.delete(patient);
    }

    Patient findOrThrow(UUID id) {
        return patientRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", id));
    }

    private void applyRequest(Patient patient, PatientRequest request) {
        patient.firstName = request.firstName();
        patient.lastName = request.lastName();
        patient.dateOfBirth = request.dateOfBirth();
        patient.gender = request.gender();
        patient.phone = request.phone();
        patient.email = request.email();
        patient.address = request.address();
    }
}

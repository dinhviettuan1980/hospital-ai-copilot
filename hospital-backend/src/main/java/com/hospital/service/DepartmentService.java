package com.hospital.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.hospital.dto.DepartmentRequest;
import com.hospital.dto.DepartmentResponse;
import com.hospital.dto.PageResponse;
import com.hospital.entity.Department;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.repository.DepartmentRepository;

@ApplicationScoped
public class DepartmentService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "code", "createdAt", "updatedAt");
    private static final String DEFAULT_SORT_FIELD = "name";

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public PageResponse<DepartmentResponse> list(String query, int page, int size, String sortBy, String sortDir) {
        Sort sort = SortSupport.build(sortBy, sortDir, SORTABLE_FIELDS, DEFAULT_SORT_FIELD);
        var panacheQuery = departmentRepository.search(query, Page.of(page, size), sort);

        List<DepartmentResponse> content = panacheQuery.list().stream()
                .map(departmentMapper::toResponse)
                .toList();

        return PageResponse.of(content, page, size, panacheQuery.count());
    }

    public DepartmentResponse get(UUID id) {
        return departmentMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("A department with code '" + request.code() + "' already exists");
        }
        if (departmentRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("A department with name '" + request.name() + "' already exists");
        }

        Department department = new Department();
        applyRequest(department, request);
        departmentRepository.persist(department);
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department department = findOrThrow(id);

        departmentRepository.findByCode(request.code())
                .filter(existing -> !existing.id.equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "A department with code '" + request.code() + "' already exists");
                });

        applyRequest(department, request);
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public void delete(UUID id) {
        Department department = findOrThrow(id);
        departmentRepository.delete(department);
    }

    Department findOrThrow(UUID id) {
        return departmentRepository.findByIdOptional(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    private void applyRequest(Department department, DepartmentRequest request) {
        department.name = request.name();
        department.code = request.code();
        department.description = request.description();
    }
}

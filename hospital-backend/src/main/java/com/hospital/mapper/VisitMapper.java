package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.VisitResponse;
import com.hospital.entity.Visit;

@ApplicationScoped
public class VisitMapper {

    private final DepartmentMapper departmentMapper;
    private final PatientMapper patientMapper;

    public VisitMapper(DepartmentMapper departmentMapper, PatientMapper patientMapper) {
        this.departmentMapper = departmentMapper;
        this.patientMapper = patientMapper;
    }

    public VisitResponse toResponse(Visit visit) {
        return new VisitResponse(
                visit.id,
                patientMapper.toSummary(visit.patient),
                departmentMapper.toSummary(visit.department),
                visit.visitDate,
                visit.reason,
                visit.status,
                visit.notes,
                visit.createdAt,
                visit.updatedAt);
    }
}

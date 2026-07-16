package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DepartmentResponse;
import com.hospital.dto.DepartmentSummary;
import com.hospital.entity.Department;

@ApplicationScoped
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.id,
                department.name,
                department.code,
                department.description,
                department.createdAt,
                department.updatedAt);
    }

    public DepartmentSummary toSummary(Department department) {
        return new DepartmentSummary(department.id, department.name, department.code);
    }
}

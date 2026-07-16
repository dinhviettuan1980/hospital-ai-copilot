package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryProjectExport;
import com.hospital.dto.DiscoveryProjectResponse;
import com.hospital.entity.DiscoveryProject;

@ApplicationScoped
public class DiscoveryProjectMapper {

    public DiscoveryProjectResponse toResponse(DiscoveryProject project, double progressPercent) {
        return new DiscoveryProjectResponse(
                project.id,
                project.projectName,
                project.hospitalName,
                project.contactPerson,
                project.contactEmail,
                project.contactPhone,
                project.surveyDate,
                project.status,
                project.notes,
                progressPercent,
                project.createdAt,
                project.updatedAt);
    }

    public DiscoveryProjectExport toExport(DiscoveryProject project) {
        return new DiscoveryProjectExport(
                project.projectName,
                project.hospitalName,
                project.contactPerson,
                project.contactEmail,
                project.contactPhone,
                project.surveyDate,
                project.status,
                project.notes);
    }
}

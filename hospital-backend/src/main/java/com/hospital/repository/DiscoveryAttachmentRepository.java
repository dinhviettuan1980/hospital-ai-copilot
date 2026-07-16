package com.hospital.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.DiscoveryAttachment;

@ApplicationScoped
public class DiscoveryAttachmentRepository implements PanacheRepositoryBase<DiscoveryAttachment, UUID> {

    public List<DiscoveryAttachment> listByProject(UUID projectId) {
        return list("project.id", projectId);
    }

    public List<DiscoveryAttachment> listByQuestion(UUID projectId, UUID questionId) {
        return list("project.id = ?1 and question.id = ?2", projectId, questionId);
    }
}

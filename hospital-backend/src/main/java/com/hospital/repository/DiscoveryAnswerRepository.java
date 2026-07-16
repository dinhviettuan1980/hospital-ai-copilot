package com.hospital.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.DiscoveryAnswer;
import com.hospital.entity.DiscoveryRiskLevel;

@ApplicationScoped
public class DiscoveryAnswerRepository implements PanacheRepositoryBase<DiscoveryAnswer, UUID> {

    public Optional<DiscoveryAnswer> findByProjectAndQuestion(UUID projectId, UUID questionId) {
        return find("project.id = ?1 and question.id = ?2", projectId, questionId).firstResultOptional();
    }

    public List<DiscoveryAnswer> listByProject(UUID projectId) {
        return list("project.id", projectId);
    }

    public long countByProject(UUID projectId) {
        return count("project.id", projectId);
    }

    public long countByRiskLevel(DiscoveryRiskLevel riskLevel) {
        return count("riskLevel", riskLevel);
    }
}

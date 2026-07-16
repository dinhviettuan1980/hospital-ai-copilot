package com.hospital.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.DiscoveryQuestion;

@ApplicationScoped
public class DiscoveryQuestionRepository implements PanacheRepositoryBase<DiscoveryQuestion, UUID> {

    public List<DiscoveryQuestion> listBySection(UUID sectionId) {
        return list("section.id", Sort.by("displayOrder"), sectionId);
    }

    public List<DiscoveryQuestion> search(String query, UUID sectionId) {
        StringBuilder jpql = new StringBuilder("1=1");
        Map<String, Object> params = new LinkedHashMap<>();
        if (query != null && !query.isBlank()) {
            jpql.append(" and (lower(title) like :query or lower(code) like :query)");
            params.put("query", "%" + query.trim().toLowerCase() + "%");
        }
        if (sectionId != null) {
            jpql.append(" and section.id = :sectionId");
            params.put("sectionId", sectionId);
        }
        return find(jpql.toString(), Sort.by("displayOrder"), params).list();
    }

    public Optional<DiscoveryQuestion> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }
}

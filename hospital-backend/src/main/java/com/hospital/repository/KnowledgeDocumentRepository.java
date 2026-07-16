package com.hospital.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.KnowledgeDocument;

@ApplicationScoped
public class KnowledgeDocumentRepository implements PanacheRepositoryBase<KnowledgeDocument, UUID> {

    public PanacheQuery<KnowledgeDocument> search(String title, UUID categoryId, Page page, Sort sort) {
        StringBuilder jpql = new StringBuilder("1=1");
        Map<String, Object> params = new LinkedHashMap<>();

        if (title != null && !title.isBlank()) {
            jpql.append(" and lower(title) like :title");
            params.put("title", "%" + title.trim().toLowerCase() + "%");
        }
        if (categoryId != null) {
            jpql.append(" and category.id = :categoryId");
            params.put("categoryId", categoryId);
        }

        return find(jpql.toString(), sort, params).page(page);
    }
}

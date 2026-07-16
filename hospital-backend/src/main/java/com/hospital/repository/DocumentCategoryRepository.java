package com.hospital.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.DocumentCategory;

@ApplicationScoped
public class DocumentCategoryRepository implements PanacheRepositoryBase<DocumentCategory, UUID> {

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    public List<DocumentCategory> listAllSorted() {
        return listAll(Sort.by("name"));
    }
}

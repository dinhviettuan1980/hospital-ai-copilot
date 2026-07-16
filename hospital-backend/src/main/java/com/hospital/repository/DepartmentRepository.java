package com.hospital.repository;

import java.util.Optional;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.Department;

@ApplicationScoped
public class DepartmentRepository implements PanacheRepositoryBase<Department, UUID> {

    public PanacheQuery<Department> search(String query, Page page, Sort sort) {
        if (query == null || query.isBlank()) {
            return findAll(sort).page(page);
        }
        String like = "%" + query.trim().toLowerCase() + "%";
        return find("lower(name) like ?1 or lower(code) like ?1", sort, like).page(page);
    }

    public Optional<Department> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public boolean existsByCode(String code) {
        return count("code", code) > 0;
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }
}

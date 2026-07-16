package com.hospital.repository;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.DiscoveryProject;

@ApplicationScoped
public class DiscoveryProjectRepository implements PanacheRepositoryBase<DiscoveryProject, UUID> {

    public PanacheQuery<DiscoveryProject> search(String query, Page page, Sort sort) {
        if (query == null || query.isBlank()) {
            return findAll(sort).page(page);
        }
        String like = "%" + query.trim().toLowerCase() + "%";
        return find("lower(projectName) like ?1 or lower(hospitalName) like ?1 or lower(contactPerson) like ?1",
                sort, like).page(page);
    }
}

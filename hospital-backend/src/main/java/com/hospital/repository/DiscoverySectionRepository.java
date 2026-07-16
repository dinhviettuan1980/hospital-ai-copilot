package com.hospital.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.DiscoverySection;

@ApplicationScoped
public class DiscoverySectionRepository implements PanacheRepositoryBase<DiscoverySection, UUID> {

    public List<DiscoverySection> listAllOrdered() {
        return listAll(Sort.by("displayOrder"));
    }

    public List<DiscoverySection> search(String query) {
        if (query == null || query.isBlank()) {
            return listAllOrdered();
        }
        String like = "%" + query.trim().toLowerCase() + "%";
        return list("lower(name) like ?1 or lower(code) like ?1", Sort.by("displayOrder"), like);
    }
}

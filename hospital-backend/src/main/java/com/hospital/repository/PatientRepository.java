package com.hospital.repository;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.Patient;

@ApplicationScoped
public class PatientRepository implements PanacheRepositoryBase<Patient, UUID> {

    public PanacheQuery<Patient> search(String query, Page page, Sort sort) {
        if (query == null || query.isBlank()) {
            return findAll(sort).page(page);
        }
        String like = "%" + query.trim().toLowerCase() + "%";
        return find(
                "lower(firstName) like ?1 or lower(lastName) like ?1 or lower(email) like ?1 or phone like ?1",
                sort, like).page(page);
    }
}

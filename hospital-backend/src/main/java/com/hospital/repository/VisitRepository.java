package com.hospital.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;

@ApplicationScoped
public class VisitRepository implements PanacheRepositoryBase<Visit, UUID> {

    public PanacheQuery<Visit> search(String query, UUID departmentId, UUID patientId, VisitStatus status,
            Page page, Sort sort) {
        StringBuilder jpql = new StringBuilder("1=1");
        Map<String, Object> params = new LinkedHashMap<>();

        if (query != null && !query.isBlank()) {
            jpql.append(" and (lower(reason) like :query or lower(patient.firstName) like :query "
                    + "or lower(patient.lastName) like :query or lower(department.name) like :query)");
            params.put("query", "%" + query.trim().toLowerCase() + "%");
        }
        if (departmentId != null) {
            jpql.append(" and department.id = :departmentId");
            params.put("departmentId", departmentId);
        }
        if (patientId != null) {
            jpql.append(" and patient.id = :patientId");
            params.put("patientId", patientId);
        }
        if (status != null) {
            jpql.append(" and status = :status");
            params.put("status", status);
        }

        return find(jpql.toString(), sort, params).page(page);
    }

    public long countToday() {
        return countOnDate(LocalDate.now());
    }

    public long countOnDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return count("visitDate >= ?1 and visitDate < ?2", start, end);
    }

    /** All visits that occurred on the given calendar day, eagerly usable for in-memory aggregation. */
    public List<Visit> listOnDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return list("visitDate >= ?1 and visitDate < ?2", start, end);
    }
}

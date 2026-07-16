package com.hospital.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.entity.Bed;

@ApplicationScoped
public class BedRepository implements PanacheRepositoryBase<Bed, UUID> {

    public long countOccupied() {
        return count("occupied", true);
    }

    public long countByDepartmentCode(String code) {
        return count("department.code = ?1", code);
    }

    public long countOccupiedByDepartmentCode(String code) {
        return count("department.code = ?1 and occupied = true", code);
    }

    public List<Bed> listByDepartmentCode(String code) {
        return list("department.code", code);
    }
}

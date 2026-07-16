package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A single bed belonging to a Department. Occupancy is the basis for the
 * Executive Dashboard's Bed/ICU Occupancy cards and the Command Center's
 * capacity alerts. Departments with no inpatient beds (e.g. Outpatient)
 * simply have zero Bed rows.
 */
@Entity
@Table(name = "bed")
public class Bed extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    public Department department;

    @NotBlank
    @Size(max = 20)
    @Column(name = "code", nullable = false, length = 20)
    public String code;

    @NotNull
    @Column(name = "occupied", nullable = false)
    public boolean occupied;
}

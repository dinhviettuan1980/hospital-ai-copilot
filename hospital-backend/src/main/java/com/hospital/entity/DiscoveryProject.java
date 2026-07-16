package com.hospital.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * One hospital discovery survey. Entirely independent of the Mini HIS
 * domain (Department/Patient/Visit) — this is pre-implementation
 * requirement-gathering data, not operational hospital data.
 */
@Entity
@Table(name = "discovery_project")
public class DiscoveryProject extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(name = "project_name", nullable = false, length = 200)
    public String projectName;

    @NotBlank
    @Size(max = 200)
    @Column(name = "hospital_name", nullable = false, length = 200)
    public String hospitalName;

    @Size(max = 150)
    @Column(name = "contact_person", length = 150)
    public String contactPerson;

    @Email
    @Size(max = 150)
    @Column(name = "contact_email", length = 150)
    public String contactEmail;

    @Size(max = 30)
    @Column(name = "contact_phone", length = 30)
    public String contactPhone;

    @Column(name = "survey_date")
    public LocalDate surveyDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public DiscoveryProjectStatus status;

    @Size(max = 4000)
    @Column(name = "notes", length = 4000)
    public String notes;
}

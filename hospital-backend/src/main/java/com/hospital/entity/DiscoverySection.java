package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A questionnaire section (e.g. "IT Landscape", "Security"). Global,
 * reusable catalog data — shared across every discovery project, not owned
 * by any one of them.
 */
@Entity
@Table(name = "discovery_section")
public class DiscoverySection extends BaseEntity {

    @NotBlank
    @Size(max = 30)
    @Column(name = "code", nullable = false, unique = true, length = 30)
    public String code;

    @NotBlank
    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    public String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    public String description;

    @NotNull
    @Column(name = "display_order", nullable = false)
    public Integer displayOrder;
}

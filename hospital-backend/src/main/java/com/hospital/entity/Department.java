package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "department")
public class Department extends BaseEntity {

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, unique = true, length = 120)
    public String name;

    @NotBlank
    @Size(max = 10)
    @Column(name = "code", nullable = false, unique = true, length = 10)
    public String code;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    public String description;
}

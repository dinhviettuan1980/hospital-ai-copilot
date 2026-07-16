package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "document_category")
public class DocumentCategory extends BaseEntity {

    @NotBlank
    @Size(max = 80)
    @Column(name = "name", nullable = false, unique = true, length = 80)
    public String name;
}

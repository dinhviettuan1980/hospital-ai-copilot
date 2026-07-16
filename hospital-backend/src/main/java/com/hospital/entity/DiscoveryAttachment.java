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
 * A file uploaded as evidence for a discovery survey — optionally tied to a
 * specific question (e.g. a FILE_ATTACHMENT question's supporting document),
 * or just attached to the project generally. Metadata in PostgreSQL, bytes
 * on the local filesystem (same pattern as KnowledgeDocument in Sprint 2).
 */
@Entity
@Table(name = "discovery_attachment")
public class DiscoveryAttachment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    public DiscoveryProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    public DiscoveryQuestion question;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false, length = 255)
    public String fileName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "content_type", nullable = false, length = 100)
    public String contentType;

    @NotNull
    @Column(name = "file_size", nullable = false)
    public long fileSize;

    @NotBlank
    @Size(max = 500)
    @Column(name = "storage_path", nullable = false, length = 500)
    public String storagePath;
}

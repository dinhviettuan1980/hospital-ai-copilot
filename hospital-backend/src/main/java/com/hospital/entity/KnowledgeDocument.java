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
 * Metadata for an uploaded knowledge document. The file itself lives on the
 * local filesystem (see hospital.knowledge.storage-path); this row only
 * tracks where to find it. No embeddings/vector search here by design —
 * this sprint only prepares the future Knowledge Base (RAG is a later Epic).
 */
@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    public String title;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    public DocumentCategory category;

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

    /** Path to the stored file on the local filesystem, relative to the configured storage root. */
    @NotBlank
    @Size(max = 500)
    @Column(name = "storage_path", nullable = false, length = 500)
    public String storagePath;
}

package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A single questionnaire question, configurable and stored in the
 * database (never hardcoded in the frontend). Belongs to exactly one
 * {@link DiscoverySection}, which doubles as its category/grouping.
 * {@code optionsJson} holds a JSON array of choice labels for
 * SINGLE_CHOICE/MULTIPLE_CHOICE questions; null for every other type.
 */
@Entity
@Table(name = "discovery_question")
public class DiscoveryQuestion extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    public DiscoverySection section;

    @NotBlank
    @Size(max = 30)
    @Column(name = "code", nullable = false, unique = true, length = 30)
    public String code;

    @NotBlank
    @Size(max = 300)
    @Column(name = "title", nullable = false, length = 300)
    public String title;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    public String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, length = 20)
    public DiscoveryAnswerType answerType;

    @Column(name = "options_json", columnDefinition = "text")
    public String optionsJson;

    @NotNull
    @Column(name = "display_order", nullable = false)
    public Integer displayOrder;
}

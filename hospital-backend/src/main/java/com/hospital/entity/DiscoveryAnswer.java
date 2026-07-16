package com.hospital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * One answer to one question within one project. At most one row per
 * (project, question) pair — saving again updates it in place, which is
 * what makes auto-save/edit-in-place on the frontend a simple upsert.
 */
@Entity
@Table(name = "discovery_answer", uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "question_id" }))
public class DiscoveryAnswer extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    public DiscoveryProject project;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    public DiscoveryQuestion question;

    @Column(name = "answer_value", columnDefinition = "text")
    public String answerValue;

    @Size(max = 1000)
    @Column(name = "comment", length = 1000)
    public String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 10)
    public DiscoveryRiskLevel riskLevel;
}

package com.hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "visit")
public class Visit extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    public Department department;

    @NotNull
    @Column(name = "visit_date", nullable = false)
    public LocalDateTime visitDate;

    @NotNull
    @Size(max = 250)
    @Column(name = "reason", nullable = false, length = 250)
    public String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public VisitStatus status;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    public String notes;

    /** Minutes the patient waited before being seen. Demo metric for the Executive Dashboard/AI Director. */
    @Column(name = "waiting_minutes")
    public Integer waitingMinutes;

    /** Demo charge amount for the visit. Not a billing system — used only to derive "Today's Revenue". */
    @Column(name = "charge", precision = 10, scale = 2)
    public BigDecimal charge;
}

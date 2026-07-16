package com.hospital.dto;

import java.time.LocalDate;

import com.hospital.entity.DiscoveryProjectStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DiscoveryProjectRequest(
        @NotBlank @Size(max = 200) String projectName,
        @NotBlank @Size(max = 200) String hospitalName,
        @Size(max = 150) String contactPerson,
        @Email @Size(max = 150) String contactEmail,
        @Size(max = 30) String contactPhone,
        LocalDate surveyDate,
        @NotNull DiscoveryProjectStatus status,
        @Size(max = 4000) String notes) {
}

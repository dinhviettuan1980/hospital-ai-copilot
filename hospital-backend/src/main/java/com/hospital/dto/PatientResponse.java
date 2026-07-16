package com.hospital.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.hospital.entity.Gender;

public record PatientResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        LocalDate dateOfBirth,
        Gender gender,
        String phone,
        String email,
        String address,
        Instant createdAt,
        Instant updatedAt) {
}

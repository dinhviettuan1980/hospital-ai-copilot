package com.hospital.dto;

import java.time.LocalDate;

import com.hospital.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record PatientRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email,
        @Size(max = 250) String address) {
}

package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.PatientResponse;
import com.hospital.dto.PatientSummary;
import com.hospital.entity.Patient;

@ApplicationScoped
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.id,
                patient.firstName,
                patient.lastName,
                patient.fullName(),
                patient.dateOfBirth,
                patient.gender,
                patient.phone,
                patient.email,
                patient.address,
                patient.createdAt,
                patient.updatedAt);
    }

    public PatientSummary toSummary(Patient patient) {
        return new PatientSummary(patient.id, patient.fullName());
    }
}

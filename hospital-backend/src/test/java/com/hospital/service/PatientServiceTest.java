package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

import com.hospital.dto.PatientRequest;
import com.hospital.dto.PatientResponse;
import com.hospital.entity.Gender;
import com.hospital.entity.Patient;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.PatientMapper;
import com.hospital.repository.PatientRepository;

class PatientServiceTest {

    private PatientRepository patientRepository;
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepository.class);
        patientService = new PatientService(patientRepository, new PatientMapper());
    }

    private PatientRequest sampleRequest() {
        return new PatientRequest("Ada", "Lovelace", LocalDate.of(1990, 1, 1), Gender.FEMALE,
                "555-000-1111", "ada@example.test", "1 Main St");
    }

    @Test
    void createPersistsPatientAndReturnsFullName() {
        PatientResponse response = patientService.create(sampleRequest());

        assertThat(response.fullName()).isEqualTo("Ada Lovelace");
        verify(patientRepository).persist(any(Patient.class));
    }

    @Test
    void getThrowsWhenPatientDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(patientRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAppliesAllFields() {
        UUID id = UUID.randomUUID();
        Patient existing = new Patient();
        existing.id = id;
        existing.firstName = "Old";
        existing.lastName = "Name";
        when(patientRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));

        PatientResponse response = patientService.update(id, sampleRequest());

        assertThat(response.firstName()).isEqualTo("Ada");
        assertThat(response.lastName()).isEqualTo("Lovelace");
        assertThat(existing.firstName).isEqualTo("Ada");
    }

    @Test
    void deleteRemovesExistingPatient() {
        UUID id = UUID.randomUUID();
        Patient existing = new Patient();
        existing.id = id;
        when(patientRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));

        patientService.delete(id);

        verify(patientRepository).delete(existing);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listMapsPagedResultsFromRepository() {
        Patient patient = new Patient();
        patient.id = UUID.randomUUID();
        patient.firstName = "Ada";
        patient.lastName = "Lovelace";

        PanacheQuery<Patient> panacheQuery = mock(PanacheQuery.class);
        when(panacheQuery.list()).thenReturn(List.of(patient));
        when(panacheQuery.count()).thenReturn(1L);
        when(patientRepository.search(any(), any(), any())).thenReturn(panacheQuery);

        var result = patientService.list("ada", 0, 20, "lastName", "asc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).fullName()).isEqualTo("Ada Lovelace");
    }
}

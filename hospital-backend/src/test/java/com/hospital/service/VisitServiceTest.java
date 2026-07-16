package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.hibernate.orm.panache.PanacheQuery;

import com.hospital.dto.VisitRequest;
import com.hospital.dto.VisitResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Patient;
import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.PatientMapper;
import com.hospital.mapper.VisitMapper;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.VisitRepository;

class VisitServiceTest {

    private VisitRepository visitRepository;
    private PatientService patientService;
    private DepartmentService departmentService;
    private VisitService visitService;

    private Patient patient;
    private Department department;

    @BeforeEach
    void setUp() {
        visitRepository = mock(VisitRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);

        patientService = new PatientService(patientRepository, new PatientMapper());
        departmentService = new DepartmentService(departmentRepository, new DepartmentMapper());
        VisitMapper visitMapper = new VisitMapper(new DepartmentMapper(), new PatientMapper());
        visitService = new VisitService(visitRepository, visitMapper, patientService, departmentService);

        patient = new Patient();
        patient.id = UUID.randomUUID();
        patient.firstName = "Ada";
        patient.lastName = "Lovelace";

        department = new Department();
        department.id = UUID.randomUUID();
        department.name = "Cardiology";
        department.code = "CARD";

        when(patientRepository.findByIdOptional(patient.id)).thenReturn(Optional.of(patient));
        when(departmentRepository.findByIdOptional(department.id)).thenReturn(Optional.of(department));
    }

    private VisitRequest sampleRequest() {
        return new VisitRequest(patient.id, department.id, LocalDateTime.now(), "Checkup",
                VisitStatus.SCHEDULED, null);
    }

    @Test
    void createResolvesPatientAndDepartmentAndPersistsVisit() {
        VisitResponse response = visitService.create(sampleRequest());

        assertThat(response.patient().id()).isEqualTo(patient.id);
        assertThat(response.department().id()).isEqualTo(department.id);
        verify(visitRepository).persist(any(Visit.class));
    }

    @Test
    void createThrowsWhenPatientDoesNotExist() {
        VisitRequest request = new VisitRequest(UUID.randomUUID(), department.id, LocalDateTime.now(),
                "Checkup", VisitStatus.SCHEDULED, null);

        assertThatThrownBy(() -> visitService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createThrowsWhenDepartmentDoesNotExist() {
        VisitRequest request = new VisitRequest(patient.id, UUID.randomUUID(), LocalDateTime.now(),
                "Checkup", VisitStatus.SCHEDULED, null);

        assertThatThrownBy(() -> visitService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getThrowsWhenVisitDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(visitRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.get(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingVisit() {
        UUID id = UUID.randomUUID();
        Visit visit = new Visit();
        visit.id = id;
        visit.patient = patient;
        visit.department = department;
        when(visitRepository.findByIdOptional(id)).thenReturn(Optional.of(visit));

        visitService.delete(id);

        verify(visitRepository).delete(visit);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listMapsPagedResultsFromRepository() {
        Visit visit = new Visit();
        visit.id = UUID.randomUUID();
        visit.patient = patient;
        visit.department = department;
        visit.visitDate = LocalDateTime.now();
        visit.reason = "Checkup";
        visit.status = VisitStatus.SCHEDULED;

        PanacheQuery<Visit> panacheQuery = mock(PanacheQuery.class);
        when(panacheQuery.list()).thenReturn(List.of(visit));
        when(panacheQuery.count()).thenReturn(1L);
        when(visitRepository.search(any(), any(), any(), any(), any(), any())).thenReturn(panacheQuery);

        var result = visitService.list(null, null, null, null, 0, 20, "visitDate", "desc");

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).reason()).isEqualTo("Checkup");
    }
}

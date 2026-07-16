package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospital.dto.AiDirectorResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Patient;
import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;
import com.hospital.repository.BedRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.VisitRepository;

class RuleBasedAiDirectorEngineTest {

    private VisitRepository visitRepository;
    private BedRepository bedRepository;
    private DepartmentRepository departmentRepository;
    private RuleBasedAiDirectorEngine engine;

    private Department emergency;
    private Department cardiology;
    private Patient patient;

    @BeforeEach
    void setUp() {
        visitRepository = mock(VisitRepository.class);
        bedRepository = mock(BedRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        engine = new RuleBasedAiDirectorEngine(visitRepository, bedRepository, departmentRepository);

        emergency = new Department();
        emergency.id = UUID.randomUUID();
        emergency.code = "ER";
        emergency.name = "Emergency";

        cardiology = new Department();
        cardiology.id = UUID.randomUUID();
        cardiology.code = "CARD";
        cardiology.name = "Cardiology";

        patient = new Patient();
        patient.id = UUID.randomUUID();

        when(visitRepository.listOnDate(any())).thenReturn(List.of());
    }

    private Visit visitFor(Department department) {
        Visit visit = new Visit();
        visit.patient = patient;
        visit.department = department;
        visit.visitDate = LocalDateTime.now();
        visit.status = VisitStatus.COMPLETED;
        visit.charge = BigDecimal.TEN;
        return visit;
    }

    @Test
    void answersIcuBedAvailability() {
        when(bedRepository.countByDepartmentCode("ICU")).thenReturn(15L);
        when(bedRepository.countOccupiedByDepartmentCode("ICU")).thenReturn(12L);

        AiDirectorResponse response = engine.answer("How many ICU beds are available?");

        assertThat(response.intent()).isEqualTo("icu-bed-availability");
        assertThat(response.answer()).contains("3 ICU beds available");
    }

    @Test
    void answersPatientsToday() {
        when(visitRepository.listOnDate(LocalDate.now())).thenReturn(List.of(visitFor(emergency), visitFor(emergency)));

        AiDirectorResponse response = engine.answer("How many patients visited today?");

        assertThat(response.intent()).isEqualTo("patients-today");
        assertThat(response.answer()).contains("1 patient");
    }

    @Test
    void answersHighestWorkloadDepartment() {
        when(departmentRepository.listAll()).thenReturn(List.of(emergency, cardiology));
        when(visitRepository.listAll()).thenReturn(List.of(visitFor(emergency), visitFor(emergency), visitFor(cardiology)));

        AiDirectorResponse response = engine.answer("Which department has the highest number of visits?");

        assertThat(response.intent()).isEqualTo("highest-workload-department");
        assertThat(response.answer()).contains("Emergency");
    }

    @Test
    void answersLowestWorkloadDepartment() {
        when(departmentRepository.listAll()).thenReturn(List.of(emergency, cardiology));
        when(visitRepository.listAll()).thenReturn(List.of(visitFor(emergency), visitFor(emergency), visitFor(cardiology)));

        AiDirectorResponse response = engine.answer("Which department has the lowest workload?");

        assertThat(response.intent()).isEqualTo("lowest-workload-department");
        assertThat(response.answer()).contains("Cardiology");
    }

    @Test
    void answersTodaysRevenue() {
        when(visitRepository.listOnDate(LocalDate.now()))
                .thenReturn(List.of(visitFor(emergency)));

        AiDirectorResponse response = engine.answer("What is today's revenue?");

        assertThat(response.intent()).isEqualTo("todays-revenue");
        assertThat(response.answer()).contains("revenue");
    }

    @Test
    void fallsBackForUnknownQuestions() {
        AiDirectorResponse response = engine.answer("What's the weather like?");

        assertThat(response.intent()).isEqualTo("fallback");
        assertThat(response.answer()).contains("don't have an answer");
    }
}

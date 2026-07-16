package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospital.dto.ExecutiveSummaryResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Patient;
import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;
import com.hospital.repository.BedRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.VisitRepository;

class DashboardServiceTest {

    private PatientRepository patientRepository;
    private DepartmentRepository departmentRepository;
    private VisitRepository visitRepository;
    private BedRepository bedRepository;
    private DashboardService dashboardService;

    private Department emergency;
    private Department surgery;

    @BeforeEach
    void setUp() {
        patientRepository = mock(PatientRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        visitRepository = mock(VisitRepository.class);
        bedRepository = mock(BedRepository.class);
        dashboardService = new DashboardService(patientRepository, departmentRepository, visitRepository, bedRepository);

        emergency = new Department();
        emergency.id = UUID.randomUUID();
        emergency.code = "ER";
        emergency.name = "Emergency";

        surgery = new Department();
        surgery.id = UUID.randomUUID();
        surgery.code = "SURG";
        surgery.name = "Surgery";
    }

    private Visit visitFor(Department department, Patient patient, Integer waitingMinutes, BigDecimal charge) {
        Visit visit = new Visit();
        visit.patient = patient;
        visit.department = department;
        visit.visitDate = LocalDateTime.now();
        visit.status = VisitStatus.COMPLETED;
        visit.waitingMinutes = waitingMinutes;
        visit.charge = charge;
        return visit;
    }

    @Test
    void summaryReturnsRawCounts() {
        when(patientRepository.count()).thenReturn(50L);
        when(departmentRepository.count()).thenReturn(5L);
        when(visitRepository.count()).thenReturn(200L);
        when(visitRepository.countToday()).thenReturn(8L);

        var response = dashboardService.summary();

        assertThat(response.totalPatients()).isEqualTo(50L);
        assertThat(response.totalDepartments()).isEqualTo(5L);
        assertThat(response.totalVisits()).isEqualTo(200L);
        assertThat(response.todaysVisits()).isEqualTo(8L);
    }

    @Test
    void executiveSummaryAggregatesTodaysData() {
        Patient patientA = new Patient();
        patientA.id = UUID.randomUUID();
        Patient patientB = new Patient();
        patientB.id = UUID.randomUUID();

        List<Visit> todaysVisits = List.of(
                visitFor(emergency, patientA, 40, BigDecimal.valueOf(200)),
                visitFor(emergency, patientA, 20, BigDecimal.valueOf(100)),
                visitFor(surgery, patientB, null, BigDecimal.valueOf(5000)));
        when(visitRepository.listOnDate(any())).thenReturn(todaysVisits);
        when(bedRepository.countOccupied()).thenReturn(30L);
        when(bedRepository.count()).thenReturn(60L);
        when(bedRepository.countOccupiedByDepartmentCode("ICU")).thenReturn(9L);
        when(bedRepository.countByDepartmentCode("ICU")).thenReturn(10L);

        ExecutiveSummaryResponse response = dashboardService.executiveSummary();

        assertThat(response.todaysPatients()).isEqualTo(2L);
        assertThat(response.todaysVisits()).isEqualTo(3L);
        assertThat(response.emergencyCases()).isEqualTo(2L);
        assertThat(response.todaysSurgeries()).isEqualTo(1L);
        assertThat(response.bedOccupancyRate()).isEqualTo(50.0);
        assertThat(response.icuOccupancyRate()).isEqualTo(90.0);
        assertThat(response.averageWaitingMinutes()).isEqualTo(30.0);
        assertThat(response.todaysRevenue()).isEqualByComparingTo(BigDecimal.valueOf(5300));
    }
}

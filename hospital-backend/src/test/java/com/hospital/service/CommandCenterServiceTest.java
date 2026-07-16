package com.hospital.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hospital.dto.AlertSeverity;
import com.hospital.dto.CommandCenterStatusResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Patient;
import com.hospital.entity.Visit;
import com.hospital.entity.VisitStatus;
import com.hospital.repository.BedRepository;
import com.hospital.repository.VisitRepository;

class CommandCenterServiceTest {

    private VisitRepository visitRepository;
    private BedRepository bedRepository;
    private CommandCenterService commandCenterService;
    private Department emergency;
    private Patient patient;

    @BeforeEach
    void setUp() {
        visitRepository = mock(VisitRepository.class);
        bedRepository = mock(BedRepository.class);
        commandCenterService = new CommandCenterService(visitRepository, bedRepository);

        emergency = new Department();
        emergency.id = UUID.randomUUID();
        emergency.code = "ER";
        emergency.name = "Emergency";

        patient = new Patient();
        patient.id = UUID.randomUUID();

        // Neutral baseline: no visits/beds anywhere unless a test overrides it.
        when(visitRepository.listOnDate(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(visitRepository.countOnDate(org.mockito.ArgumentMatchers.any())).thenReturn(0L);
    }

    private Visit visitFor(Department department, Integer waitingMinutes, BigDecimal charge) {
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
    void reportsGreenWhenNothingIsWrong() {
        when(bedRepository.countByDepartmentCode("ICU")).thenReturn(10L);
        when(bedRepository.countOccupiedByDepartmentCode("ICU")).thenReturn(2L);
        when(bedRepository.count()).thenReturn(50L);
        when(bedRepository.countOccupied()).thenReturn(10L);

        CommandCenterStatusResponse status = commandCenterService.status();

        assertThat(status.overallStatus()).isEqualTo(AlertSeverity.GREEN);
        assertThat(status.alerts()).isEmpty();
    }

    @Test
    void flagsIcuNearlyFullAsRed() {
        when(bedRepository.countByDepartmentCode("ICU")).thenReturn(10L);
        when(bedRepository.countOccupiedByDepartmentCode("ICU")).thenReturn(9L);
        when(bedRepository.count()).thenReturn(50L);
        when(bedRepository.countOccupied()).thenReturn(9L);

        CommandCenterStatusResponse status = commandCenterService.status();

        assertThat(status.overallStatus()).isEqualTo(AlertSeverity.RED);
        assertThat(status.alerts()).anySatisfy(alert -> {
            assertThat(alert.title()).isEqualTo("ICU nearly full");
            assertThat(alert.severity()).isEqualTo(AlertSeverity.RED);
        });
    }

    @Test
    void flagsHighEmergencyWaitingTime() {
        when(bedRepository.countByDepartmentCode("ICU")).thenReturn(0L);
        when(bedRepository.count()).thenReturn(0L);
        when(bedRepository.countOccupied()).thenReturn(0L);
        when(visitRepository.listOnDate(LocalDate.now()))
                .thenReturn(List.of(visitFor(emergency, 70, BigDecimal.TEN), visitFor(emergency, 80, BigDecimal.TEN)));

        CommandCenterStatusResponse status = commandCenterService.status();

        assertThat(status.alerts()).anySatisfy(alert -> {
            assertThat(alert.title()).isEqualTo("Emergency waiting time too high");
            assertThat(alert.severity()).isEqualTo(AlertSeverity.RED);
        });
    }

    @Test
    void flagsRevenueDropVsYesterday() {
        when(bedRepository.countByDepartmentCode("ICU")).thenReturn(0L);
        when(bedRepository.count()).thenReturn(0L);
        when(bedRepository.countOccupied()).thenReturn(0L);
        when(visitRepository.listOnDate(LocalDate.now()))
                .thenReturn(List.of(visitFor(emergency, 10, BigDecimal.valueOf(100))));
        when(visitRepository.listOnDate(LocalDate.now().minusDays(1)))
                .thenReturn(List.of(visitFor(emergency, 10, BigDecimal.valueOf(1000))));

        CommandCenterStatusResponse status = commandCenterService.status();

        assertThat(status.alerts()).anySatisfy(alert -> {
            assertThat(alert.title()).isEqualTo("Revenue lower than yesterday");
            assertThat(alert.severity()).isEqualTo(AlertSeverity.YELLOW);
        });
    }
}

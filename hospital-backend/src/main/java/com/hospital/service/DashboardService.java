package com.hospital.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DashboardSummaryResponse;
import com.hospital.dto.ExecutiveSummaryResponse;
import com.hospital.entity.Visit;
import com.hospital.repository.BedRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.VisitRepository;

@ApplicationScoped
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final VisitRepository visitRepository;
    private final BedRepository bedRepository;

    public DashboardService(PatientRepository patientRepository, DepartmentRepository departmentRepository,
            VisitRepository visitRepository, BedRepository bedRepository) {
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
        this.visitRepository = visitRepository;
        this.bedRepository = bedRepository;
    }

    public DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                patientRepository.count(),
                departmentRepository.count(),
                visitRepository.count(),
                visitRepository.countToday());
    }

    public ExecutiveSummaryResponse executiveSummary() {
        List<Visit> todaysVisits = visitRepository.listOnDate(LocalDate.now());

        long distinctPatients = todaysVisits.stream().map(v -> v.patient.id).distinct().count();
        long surgeries = todaysVisits.stream()
                .filter(v -> DepartmentCodes.SURGERY.equals(v.department.code))
                .count();
        long emergencyCases = todaysVisits.stream()
                .filter(v -> DepartmentCodes.EMERGENCY.equals(v.department.code))
                .count();
        double avgWaiting = todaysVisits.stream()
                .map(v -> v.waitingMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        BigDecimal revenue = todaysVisits.stream()
                .map(v -> v.charge)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExecutiveSummaryResponse(
                distinctPatients,
                todaysVisits.size(),
                occupancyRate(bedRepository.countOccupied(), totalBeds()),
                occupancyRate(bedRepository.countOccupiedByDepartmentCode(DepartmentCodes.ICU),
                        bedRepository.countByDepartmentCode(DepartmentCodes.ICU)),
                surgeries,
                emergencyCases,
                round(avgWaiting),
                revenue.setScale(2, RoundingMode.HALF_UP));
    }

    private long totalBeds() {
        return bedRepository.count();
    }

    private double occupancyRate(long occupied, long total) {
        return total == 0 ? 0.0 : round((occupied * 100.0) / total);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

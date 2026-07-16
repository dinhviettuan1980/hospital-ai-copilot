package com.hospital.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.AlertDto;
import com.hospital.dto.AlertSeverity;
import com.hospital.dto.CommandCenterStatusResponse;
import com.hospital.entity.Visit;
import com.hospital.repository.BedRepository;
import com.hospital.repository.VisitRepository;

/**
 * Simple, explainable threshold rules over today's operational data — no ML,
 * no AI. Each rule is independent and easy to read, tune, or remove.
 */
@ApplicationScoped
public class CommandCenterService {

    private static final int LOOKBACK_DAYS = 30;

    private final VisitRepository visitRepository;
    private final BedRepository bedRepository;

    public CommandCenterService(VisitRepository visitRepository, BedRepository bedRepository) {
        this.visitRepository = visitRepository;
        this.bedRepository = bedRepository;
    }

    public CommandCenterStatusResponse status() {
        List<Visit> todaysVisits = visitRepository.listOnDate(LocalDate.now());

        List<AlertDto> alerts = new ArrayList<>();
        icuOccupancyAlert().ifPresent(alerts::add);
        overallOccupancyAlert().ifPresent(alerts::add);
        emergencyWaitingTimeAlert(todaysVisits).ifPresent(alerts::add);
        revenueDropAlert().ifPresent(alerts::add);
        highVolumeAlert(todaysVisits).ifPresent(alerts::add);

        AlertSeverity overall = alerts.stream()
                .map(AlertDto::severity)
                .max(Comparator.comparingInt(this::rank))
                .orElse(AlertSeverity.GREEN);

        return new CommandCenterStatusResponse(overall, alerts);
    }

    private Optional<AlertDto> icuOccupancyAlert() {
        long total = bedRepository.countByDepartmentCode(DepartmentCodes.ICU);
        if (total == 0) {
            return Optional.empty();
        }
        long occupied = bedRepository.countOccupiedByDepartmentCode(DepartmentCodes.ICU);
        double rate = (occupied * 100.0) / total;

        if (rate >= 90) {
            return Optional.of(new AlertDto(AlertSeverity.RED, "ICU nearly full",
                    "ICU occupancy is at %.0f%% (%d of %d beds occupied).".formatted(rate, occupied, total)));
        }
        if (rate >= 75) {
            return Optional.of(new AlertDto(AlertSeverity.YELLOW, "ICU occupancy elevated",
                    "ICU occupancy is at %.0f%% (%d of %d beds occupied).".formatted(rate, occupied, total)));
        }
        return Optional.empty();
    }

    private Optional<AlertDto> overallOccupancyAlert() {
        long total = bedRepository.count();
        if (total == 0) {
            return Optional.empty();
        }
        long occupied = bedRepository.countOccupied();
        double rate = (occupied * 100.0) / total;

        if (rate >= 90) {
            return Optional.of(new AlertDto(AlertSeverity.RED, "Hospital near capacity",
                    "Overall bed occupancy is at %.0f%%.".formatted(rate)));
        }
        if (rate >= 75) {
            return Optional.of(new AlertDto(AlertSeverity.YELLOW, "Bed occupancy elevated",
                    "Overall bed occupancy is at %.0f%%.".formatted(rate)));
        }
        return Optional.empty();
    }

    private Optional<AlertDto> emergencyWaitingTimeAlert(List<Visit> todaysVisits) {
        double avgWait = todaysVisits.stream()
                .filter(v -> DepartmentCodes.EMERGENCY.equals(v.department.code))
                .map(v -> v.waitingMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        if (avgWait >= 60) {
            return Optional.of(new AlertDto(AlertSeverity.RED, "Emergency waiting time too high",
                    "Average Emergency waiting time today is %.0f minutes.".formatted(avgWait)));
        }
        if (avgWait >= 30) {
            return Optional.of(new AlertDto(AlertSeverity.YELLOW, "Emergency waiting time rising",
                    "Average Emergency waiting time today is %.0f minutes.".formatted(avgWait)));
        }
        return Optional.empty();
    }

    private Optional<AlertDto> revenueDropAlert() {
        BigDecimal today = sumCharge(visitRepository.listOnDate(LocalDate.now()));
        BigDecimal yesterday = sumCharge(visitRepository.listOnDate(LocalDate.now().minusDays(1)));

        if (yesterday.signum() > 0 && today.compareTo(yesterday) < 0) {
            double dropPercent = 100 - (today.doubleValue() * 100.0 / yesterday.doubleValue());
            return Optional.of(new AlertDto(AlertSeverity.YELLOW, "Revenue lower than yesterday",
                    "Today's revenue is %.0f%% lower than yesterday's.".formatted(dropPercent)));
        }
        return Optional.empty();
    }

    private Optional<AlertDto> highVolumeAlert(List<Visit> todaysVisits) {
        double baseline = averageDailyVisits();
        if (baseline <= 0) {
            return Optional.empty();
        }
        double ratio = todaysVisits.size() / baseline;

        if (ratio >= 1.5) {
            return Optional.of(new AlertDto(AlertSeverity.YELLOW, "High patient volume",
                    "Today's visit volume (%d) is %.0f%% above the recent daily average (%.0f).".formatted(
                            todaysVisits.size(), (ratio - 1) * 100, baseline)));
        }
        return Optional.empty();
    }

    private double averageDailyVisits() {
        long total = 0;
        for (int i = 1; i <= LOOKBACK_DAYS; i++) {
            total += visitRepository.countOnDate(LocalDate.now().minusDays(i));
        }
        return total / (double) LOOKBACK_DAYS;
    }

    private BigDecimal sumCharge(List<Visit> visits) {
        return visits.stream()
                .map(v -> v.charge)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int rank(AlertSeverity severity) {
        return switch (severity) {
            case GREEN -> 0;
            case YELLOW -> 1;
            case RED -> 2;
        };
    }
}

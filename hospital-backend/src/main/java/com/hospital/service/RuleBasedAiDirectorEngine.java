package com.hospital.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.AiDirectorResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Visit;
import com.hospital.repository.BedRepository;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.VisitRepository;

/**
 * Demo AI Director: no LLM. Questions are matched against a small, ordered
 * set of keyword-based rules, and answers are computed from live data via
 * the existing repositories — the same "SQL + business logic" a real
 * hospital director's question would need answered.
 */
@ApplicationScoped
public class RuleBasedAiDirectorEngine implements AiDirectorEngine {

    private record Rule(String intent, Predicate<String> matches, Function<String, AiDirectorResponse> handle) {
    }

    private final VisitRepository visitRepository;
    private final BedRepository bedRepository;
    private final DepartmentRepository departmentRepository;
    private final List<Rule> rules;

    public RuleBasedAiDirectorEngine(VisitRepository visitRepository, BedRepository bedRepository,
            DepartmentRepository departmentRepository) {
        this.visitRepository = visitRepository;
        this.bedRepository = bedRepository;
        this.departmentRepository = departmentRepository;
        this.rules = List.of(
                new Rule("icu-bed-availability",
                        q -> q.contains("icu") && (q.contains("available") || q.contains("free") || q.contains("bed")),
                        q -> icuBedAvailability()),
                new Rule("patients-today",
                        q -> q.contains("patient") && q.contains("today"),
                        q -> patientsToday()),
                new Rule("highest-workload-department",
                        q -> q.contains("highest") && (q.contains("visit") || q.contains("workload")),
                        q -> departmentByWorkload(true)),
                new Rule("lowest-workload-department",
                        q -> q.contains("lowest") && (q.contains("visit") || q.contains("workload")),
                        q -> departmentByWorkload(false)),
                new Rule("todays-revenue",
                        q -> q.contains("revenue"),
                        q -> todaysRevenue()),
                new Rule("todays-summary",
                        q -> q.contains("summary"),
                        q -> todaysSummary()));
    }

    @Override
    public AiDirectorResponse answer(String question) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT).trim();
        return rules.stream()
                .filter(rule -> rule.matches().test(normalized))
                .findFirst()
                .map(rule -> rule.handle().apply(normalized))
                .orElseGet(this::fallback);
    }

    private AiDirectorResponse icuBedAvailability() {
        long total = bedRepository.countByDepartmentCode(DepartmentCodes.ICU);
        long occupied = bedRepository.countOccupiedByDepartmentCode(DepartmentCodes.ICU);
        long available = total - occupied;

        String answer = total == 0
                ? "There is no ICU bed data available yet."
                : "There are %d ICU beds available out of %d total.".formatted(available, total);

        return new AiDirectorResponse(answer, "icu-bed-availability",
                Map.of("available", available, "occupied", occupied, "total", total));
    }

    private AiDirectorResponse patientsToday() {
        List<Visit> todaysVisits = visitRepository.listOnDate(LocalDate.now());
        long distinctPatients = todaysVisits.stream().map(v -> v.patient.id).distinct().count();

        String answer = "%d patient(s) have visited the hospital today, across %d visit(s)."
                .formatted(distinctPatients, todaysVisits.size());

        return new AiDirectorResponse(answer, "patients-today",
                Map.of("patients", distinctPatients, "visits", todaysVisits.size()));
    }

    private AiDirectorResponse departmentByWorkload(boolean highest) {
        Map<String, Long> visitsByDepartment = visitCountsByDepartment();
        if (visitsByDepartment.isEmpty()) {
            return new AiDirectorResponse("There is no visit data yet.", "department-workload", Map.of());
        }

        Comparator<Map.Entry<String, Long>> byCount = Map.Entry.comparingByValue();
        Map.Entry<String, Long> entry = highest
                ? visitsByDepartment.entrySet().stream().max(byCount).orElseThrow()
                : visitsByDepartment.entrySet().stream().min(byCount).orElseThrow();

        String qualifier = highest ? "the highest number of visits" : "the lowest workload";
        String answer = "%s has %s, with %d visit(s) recorded."
                .formatted(entry.getKey(), qualifier, entry.getValue());

        return new AiDirectorResponse(answer, highest ? "highest-workload-department" : "lowest-workload-department",
                Map.of("department", entry.getKey(), "visits", entry.getValue()));
    }

    private AiDirectorResponse todaysRevenue() {
        BigDecimal revenue = sumCharge(visitRepository.listOnDate(LocalDate.now()));
        String answer = "Today's revenue so far is $%,.2f.".formatted(revenue);
        return new AiDirectorResponse(answer, "todays-revenue", Map.of("revenue", revenue));
    }

    private AiDirectorResponse todaysSummary() {
        List<Visit> todaysVisits = visitRepository.listOnDate(LocalDate.now());
        long distinctPatients = todaysVisits.stream().map(v -> v.patient.id).distinct().count();
        BigDecimal revenue = sumCharge(todaysVisits);

        long icuTotal = bedRepository.countByDepartmentCode(DepartmentCodes.ICU);
        long icuOccupied = bedRepository.countOccupiedByDepartmentCode(DepartmentCodes.ICU);
        long bedTotal = bedRepository.count();
        long bedOccupied = bedRepository.countOccupied();

        String answer = ("Today's hospital summary: %d patient(s) seen across %d visit(s). "
                + "Overall bed occupancy is %s, ICU occupancy is %s. Revenue so far: $%,.2f.")
                .formatted(distinctPatients, todaysVisits.size(),
                        percent(bedOccupied, bedTotal), percent(icuOccupied, icuTotal), revenue);

        return new AiDirectorResponse(answer, "todays-summary", Map.of(
                "patients", distinctPatients,
                "visits", todaysVisits.size(),
                "bedOccupancyRate", rate(bedOccupied, bedTotal),
                "icuOccupancyRate", rate(icuOccupied, icuTotal),
                "revenue", revenue));
    }

    private AiDirectorResponse fallback() {
        String answer = "I don't have an answer for that yet. Try asking things like: "
                + "\"How many ICU beds are available?\", \"How many patients visited today?\", "
                + "\"Which department has the highest number of visits?\", "
                + "\"Which department has the lowest workload?\", \"What is today's revenue?\", "
                + "or \"Show today's hospital summary.\"";
        return new AiDirectorResponse(answer, "fallback", null);
    }

    private Map<String, Long> visitCountsByDepartment() {
        Map<String, Long> counts = new HashMap<>();
        for (Department department : departmentRepository.listAll()) {
            counts.put(department.name, 0L);
        }
        for (Visit visit : visitRepository.listAll()) {
            counts.merge(visit.department.name, 1L, Long::sum);
        }
        return counts;
    }

    private BigDecimal sumCharge(List<Visit> visits) {
        return visits.stream()
                .map(v -> v.charge)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String percent(long part, long total) {
        return total == 0 ? "n/a" : "%.0f%%".formatted(rate(part, total));
    }

    private double rate(long part, long total) {
        return total == 0 ? 0.0 : (part * 100.0) / total;
    }
}

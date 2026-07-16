package com.hospital.dto;

import java.math.BigDecimal;

public record ExecutiveSummaryResponse(
        long todaysPatients,
        long todaysVisits,
        double bedOccupancyRate,
        double icuOccupancyRate,
        long todaysSurgeries,
        long emergencyCases,
        double averageWaitingMinutes,
        BigDecimal todaysRevenue) {
}

package com.hospital.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.DashboardSummaryResponse;
import com.hospital.dto.ExecutiveSummaryResponse;
import com.hospital.service.DashboardService;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GET
    @Path("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.summary();
    }

    @GET
    @Path("/executive-summary")
    public ExecutiveSummaryResponse executiveSummary() {
        return dashboardService.executiveSummary();
    }
}

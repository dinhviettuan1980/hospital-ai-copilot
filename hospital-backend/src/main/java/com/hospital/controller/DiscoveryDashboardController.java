package com.hospital.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.DiscoveryDashboardSummaryResponse;
import com.hospital.service.DiscoveryDashboardService;

@Path("/api/discovery/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Discovery Dashboard")
public class DiscoveryDashboardController {

    private final DiscoveryDashboardService discoveryDashboardService;

    public DiscoveryDashboardController(DiscoveryDashboardService discoveryDashboardService) {
        this.discoveryDashboardService = discoveryDashboardService;
    }

    @GET
    @Path("/summary")
    public DiscoveryDashboardSummaryResponse summary() {
        return discoveryDashboardService.summary();
    }
}

package com.hospital.controller;

import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.DiscoveryQuestionResponse;
import com.hospital.service.DiscoverySurveyService;

/**
 * The one Discovery Survey route that is NOT nested under
 * /api/discovery/projects/{projectId} — see DiscoveryProjectController for
 * the sections/questions/answer sub-resources, which live there instead
 * for routing reasons (see that class's Javadoc).
 */
@Path("/api/discovery/questions")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Discovery Survey")
public class DiscoverySurveyController {

    private final DiscoverySurveyService discoverySurveyService;

    public DiscoverySurveyController(DiscoverySurveyService discoverySurveyService) {
        this.discoverySurveyService = discoverySurveyService;
    }

    @GET
    public List<DiscoveryQuestionResponse> searchQuestions(
            @QueryParam("q") String query,
            @QueryParam("sectionId") UUID sectionId) {
        return discoverySurveyService.searchQuestions(query, sectionId);
    }
}

package com.hospital.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.DiscoveryAnswerRequest;
import com.hospital.dto.DiscoveryAnswerResponse;
import com.hospital.dto.DiscoveryAttachmentResponse;
import com.hospital.dto.DiscoveryAttachmentUploadForm;
import com.hospital.dto.DiscoveryProjectRequest;
import com.hospital.dto.DiscoveryProjectResponse;
import com.hospital.dto.DiscoverySectionProgressResponse;
import com.hospital.dto.DiscoverySurveyExport;
import com.hospital.dto.DiscoveryQuestionWithAnswerResponse;
import com.hospital.dto.PageResponse;
import com.hospital.service.DiscoveryAttachmentService;
import com.hospital.service.DiscoveryExportService;
import com.hospital.service.DiscoveryImportService;
import com.hospital.service.DiscoveryProjectService;
import com.hospital.service.DiscoverySurveyService;

/**
 * All routes under /api/discovery/projects live in this single class,
 * including the nested sections/questions/answer/attachment sub-resources.
 * RESTEasy Reactive was observed to 404 nested paths declared under this
 * prefix from a *different* resource class even when the class-level
 * {@code @Path} values were distinct but overlapping (see git history for
 * the split-controller version) — consolidating avoids that entirely.
 */
@Path("/api/discovery/projects")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Discovery Projects")
public class DiscoveryProjectController {

    private final DiscoveryProjectService discoveryProjectService;
    private final DiscoveryExportService discoveryExportService;
    private final DiscoveryImportService discoveryImportService;
    private final DiscoverySurveyService discoverySurveyService;
    private final DiscoveryAttachmentService discoveryAttachmentService;

    public DiscoveryProjectController(DiscoveryProjectService discoveryProjectService,
            DiscoveryExportService discoveryExportService, DiscoveryImportService discoveryImportService,
            DiscoverySurveyService discoverySurveyService, DiscoveryAttachmentService discoveryAttachmentService) {
        this.discoveryProjectService = discoveryProjectService;
        this.discoveryExportService = discoveryExportService;
        this.discoveryImportService = discoveryImportService;
        this.discoverySurveyService = discoverySurveyService;
        this.discoveryAttachmentService = discoveryAttachmentService;
    }

    @GET
    public PageResponse<DiscoveryProjectResponse> list(
            @QueryParam("q") String query,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDir") @DefaultValue("desc") String sortDir) {
        return discoveryProjectService.list(query, page, size, sortBy, sortDir);
    }

    @GET
    @Path("/{id}")
    public DiscoveryProjectResponse get(@PathParam("id") UUID id) {
        return discoveryProjectService.get(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid DiscoveryProjectRequest request) {
        DiscoveryProjectResponse created = discoveryProjectService.create(request);
        return Response.created(URI.create("/api/discovery/projects/" + created.id())).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public DiscoveryProjectResponse update(@PathParam("id") UUID id, @Valid DiscoveryProjectRequest request) {
        return discoveryProjectService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        discoveryProjectService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/export")
    public DiscoverySurveyExport export(@PathParam("id") UUID id) {
        return discoveryExportService.export(id);
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response importSurvey(DiscoverySurveyExport export) {
        DiscoveryProjectResponse created = discoveryImportService.importSurvey(export);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    // --- Survey workspace: sections, questions, answers -----------------

    @GET
    @Path("/{projectId}/sections")
    public List<DiscoverySectionProgressResponse> listSections(@PathParam("projectId") UUID projectId) {
        return discoverySurveyService.listSectionsWithProgress(projectId);
    }

    @GET
    @Path("/{projectId}/sections/{sectionId}/questions")
    public List<DiscoveryQuestionWithAnswerResponse> listQuestions(@PathParam("projectId") UUID projectId,
            @PathParam("sectionId") UUID sectionId) {
        return discoverySurveyService.listQuestions(projectId, sectionId);
    }

    @PUT
    @Path("/{projectId}/questions/{questionId}/answer")
    @Consumes(MediaType.APPLICATION_JSON)
    public DiscoveryAnswerResponse saveAnswer(@PathParam("projectId") UUID projectId,
            @PathParam("questionId") UUID questionId, @Valid DiscoveryAnswerRequest request) {
        return discoverySurveyService.saveAnswer(projectId, questionId, request);
    }

    // --- Attachments (project-scoped) ------------------------------------

    @GET
    @Path("/{projectId}/attachments")
    public List<DiscoveryAttachmentResponse> listAttachments(@PathParam("projectId") UUID projectId) {
        return discoveryAttachmentService.listByProject(projectId);
    }

    @POST
    @Path("/{projectId}/attachments")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadAttachment(@PathParam("projectId") UUID projectId,
            @BeanParam DiscoveryAttachmentUploadForm form) {
        UUID questionId = (form.questionId == null || form.questionId.isBlank()) ? null
                : UUID.fromString(form.questionId);
        DiscoveryAttachmentResponse created = discoveryAttachmentService.upload(projectId, questionId, form.file);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}

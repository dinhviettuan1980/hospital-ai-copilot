package com.hospital.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;
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

import com.hospital.dto.PageResponse;
import com.hospital.dto.VisitRequest;
import com.hospital.dto.VisitResponse;
import com.hospital.entity.VisitStatus;
import com.hospital.service.VisitService;

@Path("/api/visits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Visits")
public class VisitController {

    private final VisitService visitService;

    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GET
    public PageResponse<VisitResponse> list(
            @QueryParam("q") String query,
            @QueryParam("departmentId") UUID departmentId,
            @QueryParam("patientId") UUID patientId,
            @QueryParam("status") VisitStatus status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDir") @DefaultValue("desc") String sortDir) {
        return visitService.list(query, departmentId, patientId, status, page, size, sortBy, sortDir);
    }

    @GET
    @Path("/{id}")
    public VisitResponse get(@PathParam("id") UUID id) {
        return visitService.get(id);
    }

    @POST
    public Response create(@Valid VisitRequest request) {
        VisitResponse created = visitService.create(request);
        return Response.created(URI.create("/api/visits/" + created.id())).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public VisitResponse update(@PathParam("id") UUID id, @Valid VisitRequest request) {
        return visitService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        visitService.delete(id);
        return Response.noContent().build();
    }
}

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
import com.hospital.dto.PatientRequest;
import com.hospital.dto.PatientResponse;
import com.hospital.service.PatientService;

@Path("/api/patients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GET
    public PageResponse<PatientResponse> list(
            @QueryParam("q") String query,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir) {
        return patientService.list(query, page, size, sortBy, sortDir);
    }

    @GET
    @Path("/{id}")
    public PatientResponse get(@PathParam("id") UUID id) {
        return patientService.get(id);
    }

    @POST
    public Response create(@Valid PatientRequest request) {
        PatientResponse created = patientService.create(request);
        return Response.created(URI.create("/api/patients/" + created.id())).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public PatientResponse update(@PathParam("id") UUID id, @Valid PatientRequest request) {
        return patientService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        patientService.delete(id);
        return Response.noContent().build();
    }
}

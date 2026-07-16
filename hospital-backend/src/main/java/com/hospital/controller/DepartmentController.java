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

import com.hospital.dto.DepartmentRequest;
import com.hospital.dto.DepartmentResponse;
import com.hospital.dto.PageResponse;
import com.hospital.service.DepartmentService;

@Path("/api/departments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GET
    public PageResponse<DepartmentResponse> list(
            @QueryParam("q") String query,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDir") @DefaultValue("asc") String sortDir) {
        return departmentService.list(query, page, size, sortBy, sortDir);
    }

    @GET
    @Path("/{id}")
    public DepartmentResponse get(@PathParam("id") UUID id) {
        return departmentService.get(id);
    }

    @POST
    public Response create(@Valid DepartmentRequest request) {
        DepartmentResponse created = departmentService.create(request);
        return Response.created(URI.create("/api/departments/" + created.id())).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public DepartmentResponse update(@PathParam("id") UUID id, @Valid DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        departmentService.delete(id);
        return Response.noContent().build();
    }
}

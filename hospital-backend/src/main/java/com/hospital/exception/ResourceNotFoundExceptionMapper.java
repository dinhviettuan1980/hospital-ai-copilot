package com.hospital.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.hospital.dto.ErrorResponse;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.NOT_FOUND.getStatusCode(),
                "Not Found",
                exception.getMessage(),
                uriInfo.getPath());
        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}

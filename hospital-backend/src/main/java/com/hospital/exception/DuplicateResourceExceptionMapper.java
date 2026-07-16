package com.hospital.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.hospital.dto.ErrorResponse;

@Provider
public class DuplicateResourceExceptionMapper implements ExceptionMapper<DuplicateResourceException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(DuplicateResourceException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                exception.getMessage(),
                uriInfo.getPath());
        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}

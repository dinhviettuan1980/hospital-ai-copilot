package com.hospital.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.hospital.dto.ErrorResponse;

@Provider
public class InvalidFileExceptionMapper implements ExceptionMapper<InvalidFileException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(InvalidFileException exception) {
        ErrorResponse body = ErrorResponse.of(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Invalid File",
                exception.getMessage(),
                uriInfo.getPath());
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}

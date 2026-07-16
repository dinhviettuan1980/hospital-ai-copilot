package com.hospital.exception;

import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.hospital.dto.ErrorResponse;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        ErrorResponse body = ErrorResponse.of(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Validation Failed",
                "One or more fields are invalid",
                uriInfo.getPath(),
                details);
        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}

package com.hospital.exception;

import org.jboss.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.hospital.dto.ErrorResponse;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException webException) {
            return webException.getResponse();
        }

        if (exception instanceof IllegalArgumentException) {
            ErrorResponse body = ErrorResponse.of(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "Bad Request",
                    exception.getMessage(),
                    uriInfo.getPath());
            return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
        }

        LOG.error("Unhandled exception", exception);
        ErrorResponse body = ErrorResponse.of(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "An unexpected error occurred",
                uriInfo.getPath());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}

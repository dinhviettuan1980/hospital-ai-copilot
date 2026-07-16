package com.hospital.controller;

import java.util.UUID;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.entity.DiscoveryAttachment;
import com.hospital.service.DiscoveryAttachmentService;

/**
 * Attachment routes that are NOT nested under /api/discovery/projects — see
 * DiscoveryProjectController for list/upload, which live there instead for
 * routing reasons (see that class's Javadoc).
 */
@Path("/api/discovery/attachments")
@Tag(name = "Discovery Attachments")
public class DiscoveryAttachmentController {

    private final DiscoveryAttachmentService discoveryAttachmentService;

    public DiscoveryAttachmentController(DiscoveryAttachmentService discoveryAttachmentService) {
        this.discoveryAttachmentService = discoveryAttachmentService;
    }

    @GET
    @Path("/{id}/download")
    public Response download(@PathParam("id") UUID id) {
        DiscoveryAttachment attachment = discoveryAttachmentService.findOrThrow(id);
        java.io.File file = discoveryAttachmentService.resolveStoredFile(attachment).toFile();
        return Response.ok(file)
                .header("Content-Type", attachment.contentType)
                .header("Content-Disposition", "attachment; filename=\"" + attachment.fileName + "\"")
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        discoveryAttachmentService.delete(id);
        return Response.noContent().build();
    }
}

package com.hospital.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.DocumentCategoryRequest;
import com.hospital.dto.DocumentCategoryResponse;
import com.hospital.dto.DocumentUploadForm;
import com.hospital.dto.KnowledgeDocumentResponse;
import com.hospital.dto.PageResponse;
import com.hospital.entity.KnowledgeDocument;
import com.hospital.service.DocumentCategoryService;
import com.hospital.service.KnowledgeDocumentService;

@Path("/api/knowledge")
@Tag(name = "Knowledge Center")
public class KnowledgeController {

    private final DocumentCategoryService documentCategoryService;
    private final KnowledgeDocumentService knowledgeDocumentService;

    public KnowledgeController(DocumentCategoryService documentCategoryService,
            KnowledgeDocumentService knowledgeDocumentService) {
        this.documentCategoryService = documentCategoryService;
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocumentCategoryResponse> listCategories() {
        return documentCategoryService.list();
    }

    @POST
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCategory(@Valid DocumentCategoryRequest request) {
        DocumentCategoryResponse created = documentCategoryService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON)
    public PageResponse<KnowledgeDocumentResponse> listDocuments(
            @QueryParam("title") String title,
            @QueryParam("categoryId") UUID categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return knowledgeDocumentService.list(title, categoryId, page, size);
    }

    @POST
    @Path("/documents")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@BeanParam DocumentUploadForm form) {
        UUID categoryId = UUID.fromString(form.categoryId);
        KnowledgeDocumentResponse created = knowledgeDocumentService.upload(form.title, categoryId, form.file);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/documents/{id}/download")
    public Response download(@PathParam("id") UUID id) {
        KnowledgeDocument document = knowledgeDocumentService.findOrThrow(id);
        java.io.File file = knowledgeDocumentService.resolveStoredFile(document).toFile();
        return Response.ok(file)
                .header("Content-Type", document.contentType)
                .header("Content-Disposition", "attachment; filename=\"" + document.fileName + "\"")
                .build();
    }

    @DELETE
    @Path("/documents/{id}")
    public Response deleteDocument(@PathParam("id") UUID id) {
        knowledgeDocumentService.delete(id);
        return Response.noContent().build();
    }
}

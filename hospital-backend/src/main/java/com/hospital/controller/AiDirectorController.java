package com.hospital.controller;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.AiDirectorRequest;
import com.hospital.dto.AiDirectorResponse;
import com.hospital.service.AiDirectorEngine;

@Path("/api/ai-director")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "AI Director")
public class AiDirectorController {

    private final AiDirectorEngine aiDirectorEngine;

    public AiDirectorController(AiDirectorEngine aiDirectorEngine) {
        this.aiDirectorEngine = aiDirectorEngine;
    }

    @POST
    @Path("/ask")
    public AiDirectorResponse ask(@Valid AiDirectorRequest request) {
        return aiDirectorEngine.answer(request.question());
    }
}

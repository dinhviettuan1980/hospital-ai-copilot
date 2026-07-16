package com.hospital.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.hospital.dto.CommandCenterStatusResponse;
import com.hospital.service.CommandCenterService;

@Path("/api/command-center")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Command Center")
public class CommandCenterController {

    private final CommandCenterService commandCenterService;

    public CommandCenterController(CommandCenterService commandCenterService) {
        this.commandCenterService = commandCenterService;
    }

    @GET
    @Path("/status")
    public CommandCenterStatusResponse status() {
        return commandCenterService.status();
    }
}

package com.hospital.dto;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.ws.rs.core.MediaType;

public class DocumentUploadForm {

    @RestForm
    @PartType(MediaType.TEXT_PLAIN)
    public String title;

    @RestForm
    @PartType(MediaType.TEXT_PLAIN)
    public String categoryId;

    @RestForm("file")
    public FileUpload file;
}

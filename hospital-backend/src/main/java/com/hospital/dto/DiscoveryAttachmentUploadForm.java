package com.hospital.dto;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.ws.rs.core.MediaType;

public class DiscoveryAttachmentUploadForm {

    /** Optional — ties the attachment to a specific question (e.g. its FILE_ATTACHMENT answer). */
    @RestForm
    @PartType(MediaType.TEXT_PLAIN)
    public String questionId;

    @RestForm("file")
    public FileUpload file;
}

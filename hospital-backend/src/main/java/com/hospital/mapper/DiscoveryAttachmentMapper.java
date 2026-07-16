package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryAttachmentExport;
import com.hospital.dto.DiscoveryAttachmentResponse;
import com.hospital.entity.DiscoveryAttachment;

@ApplicationScoped
public class DiscoveryAttachmentMapper {

    public DiscoveryAttachmentResponse toResponse(DiscoveryAttachment attachment) {
        return new DiscoveryAttachmentResponse(
                attachment.id,
                attachment.question != null ? attachment.question.id : null,
                attachment.fileName,
                attachment.contentType,
                attachment.fileSize,
                attachment.createdAt);
    }

    public DiscoveryAttachmentExport toExport(DiscoveryAttachment attachment) {
        return new DiscoveryAttachmentExport(attachment.fileName, attachment.contentType, attachment.fileSize);
    }
}

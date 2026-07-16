package com.hospital.dto;

/** Metadata only — file bytes live on the filesystem and are not embedded in the export JSON. */
public record DiscoveryAttachmentExport(String fileName, String contentType, long fileSize) {
}

package com.hospital.dto;

import java.util.List;
import java.util.UUID;

import com.hospital.entity.DiscoveryAnswerType;

public record DiscoveryQuestionWithAnswerResponse(
        UUID id,
        String code,
        String title,
        String description,
        DiscoveryAnswerType answerType,
        List<String> options,
        int displayOrder,
        DiscoveryAnswerResponse answer,
        List<DiscoveryAttachmentResponse> attachments) {
}

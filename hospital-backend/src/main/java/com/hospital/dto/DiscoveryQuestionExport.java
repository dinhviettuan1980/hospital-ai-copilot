package com.hospital.dto;

import java.util.List;

import com.hospital.entity.DiscoveryAnswerType;

public record DiscoveryQuestionExport(
        String code,
        String title,
        String description,
        DiscoveryAnswerType answerType,
        List<String> options,
        DiscoveryAnswerExport answer,
        List<DiscoveryAttachmentExport> attachments) {
}

package com.hospital.mapper;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryAnswerResponse;
import com.hospital.dto.DiscoveryAttachmentResponse;
import com.hospital.dto.DiscoveryQuestionExport;
import com.hospital.dto.DiscoveryQuestionResponse;
import com.hospital.dto.DiscoveryQuestionWithAnswerResponse;
import com.hospital.entity.DiscoveryQuestion;

@ApplicationScoped
public class DiscoveryQuestionMapper {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public DiscoveryQuestionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public DiscoveryQuestionResponse toResponse(DiscoveryQuestion question) {
        return new DiscoveryQuestionResponse(
                question.id,
                question.code,
                question.title,
                question.description,
                question.answerType,
                parseOptions(question.optionsJson),
                question.displayOrder,
                question.section.id,
                question.section.name);
    }

    public DiscoveryQuestionWithAnswerResponse toResponseWithAnswer(DiscoveryQuestion question,
            DiscoveryAnswerResponse answer, List<DiscoveryAttachmentResponse> attachments) {
        return new DiscoveryQuestionWithAnswerResponse(
                question.id,
                question.code,
                question.title,
                question.description,
                question.answerType,
                parseOptions(question.optionsJson),
                question.displayOrder,
                answer,
                attachments);
    }

    public DiscoveryQuestionExport toExport(DiscoveryQuestion question, com.hospital.dto.DiscoveryAnswerExport answer,
            List<com.hospital.dto.DiscoveryAttachmentExport> attachments) {
        return new DiscoveryQuestionExport(
                question.code,
                question.title,
                question.description,
                question.answerType,
                parseOptions(question.optionsJson),
                answer,
                attachments);
    }

    public String writeOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize question options", e);
        }
    }

    public List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(optionsJson, STRING_LIST);
        } catch (Exception e) {
            return List.of();
        }
    }
}

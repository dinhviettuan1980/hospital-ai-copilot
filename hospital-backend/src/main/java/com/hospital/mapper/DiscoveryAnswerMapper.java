package com.hospital.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.hospital.dto.DiscoveryAnswerExport;
import com.hospital.dto.DiscoveryAnswerResponse;
import com.hospital.entity.DiscoveryAnswer;

@ApplicationScoped
public class DiscoveryAnswerMapper {

    public DiscoveryAnswerResponse toResponse(DiscoveryAnswer answer) {
        if (answer == null) {
            return null;
        }
        return new DiscoveryAnswerResponse(answer.answerValue, answer.comment, answer.riskLevel, answer.updatedAt);
    }

    public DiscoveryAnswerExport toExport(DiscoveryAnswer answer) {
        if (answer == null) {
            return null;
        }
        return new DiscoveryAnswerExport(answer.answerValue, answer.comment, answer.riskLevel);
    }
}

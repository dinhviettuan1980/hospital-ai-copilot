package com.hospital.service;

import com.hospital.dto.AiDirectorResponse;

/**
 * Answers a Hospital Director's natural-language question. The Sprint 2 demo
 * implementation ({@link RuleBasedAiDirectorEngine}) uses keyword matching
 * and SQL/aggregation over existing data — no LLM. A future implementation
 * (e.g. backed by RAG + Text-to-SQL) can implement this same interface
 * without any change to {@code AiDirectorController} or the frontend.
 */
public interface AiDirectorEngine {

    AiDirectorResponse answer(String question);
}

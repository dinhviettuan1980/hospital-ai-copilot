package com.hospital.dto;

/**
 * {@code data} is a small, JSON-serializable record/map of the figures behind
 * {@code answer}, kept separate from the natural-language text so a future
 * LLM-backed engine can populate the same contract without a UI change.
 */
public record AiDirectorResponse(String answer, String intent, Object data) {
}

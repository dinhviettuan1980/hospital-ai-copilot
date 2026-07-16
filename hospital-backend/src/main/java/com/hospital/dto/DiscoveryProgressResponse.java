package com.hospital.dto;

public record DiscoveryProgressResponse(int totalQuestions, int answeredQuestions, double percent) {
}

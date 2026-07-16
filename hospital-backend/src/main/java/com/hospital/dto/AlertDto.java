package com.hospital.dto;

public record AlertDto(AlertSeverity severity, String title, String message) {
}

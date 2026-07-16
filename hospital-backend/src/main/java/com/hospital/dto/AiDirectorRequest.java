package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiDirectorRequest(@NotBlank @Size(max = 500) String question) {
}

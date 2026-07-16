package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentCategoryRequest(@NotBlank @Size(max = 80) String name) {
}

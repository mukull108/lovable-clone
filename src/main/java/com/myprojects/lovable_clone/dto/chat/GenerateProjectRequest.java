package com.myprojects.lovable_clone.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record GenerateProjectRequest(
        Long chatSessionId,
        @NotBlank String prompt
) {
}

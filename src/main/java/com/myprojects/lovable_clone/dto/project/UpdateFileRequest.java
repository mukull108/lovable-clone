package com.myprojects.lovable_clone.dto.project;

import jakarta.validation.constraints.NotBlank;

public record UpdateFileRequest(
        @NotBlank String content
) {
}

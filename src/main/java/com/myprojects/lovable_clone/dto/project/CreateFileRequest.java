package com.myprojects.lovable_clone.dto.project;

import jakarta.validation.constraints.NotBlank;

public record CreateFileRequest(
        @NotBlank String path,
        @NotBlank String content
) {
}

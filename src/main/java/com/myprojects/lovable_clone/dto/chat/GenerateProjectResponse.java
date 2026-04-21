package com.myprojects.lovable_clone.dto.chat;

import java.util.List;

public record GenerateProjectResponse(
        Long chatSessionId,
        Long userMessageId,
        Long assistantMessageId,
        String summary,
        List<GeneratedFileResponse> files
) {
}

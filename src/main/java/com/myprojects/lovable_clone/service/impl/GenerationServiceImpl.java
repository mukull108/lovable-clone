package com.myprojects.lovable_clone.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myprojects.lovable_clone.config.AiGenerationProperties;
import com.myprojects.lovable_clone.dto.chat.GenerateProjectRequest;
import com.myprojects.lovable_clone.dto.chat.GenerateProjectResponse;
import com.myprojects.lovable_clone.dto.chat.GeneratedFileResponse;
import com.myprojects.lovable_clone.entity.ChatMessage;
import com.myprojects.lovable_clone.entity.ChatSession;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectFile;
import com.myprojects.lovable_clone.entity.UsageLog;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.MessageRole;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.exceptions.BadRequestException;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.ChatMessageRepository;
import com.myprojects.lovable_clone.repository.ChatSessionRepository;
import com.myprojects.lovable_clone.repository.ProjectFileRepository;
import com.myprojects.lovable_clone.repository.UsageLogRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.security.ProjectAuthorizationService;
import com.myprojects.lovable_clone.service.GenerationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GenerationServiceImpl implements GenerationService {
    private final ProjectAuthorizationService projectAuthorizationService;
    private final AuthUtils authUtils;
    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProjectFileRepository projectFileRepository;
    private final UsageLogRepository usageLogRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final AiGenerationProperties aiGenerationProperties;
    private final ObjectMapper objectMapper;

    @Override
    public GenerateProjectResponse generateProject(Long projectId, GenerateProjectRequest request) {
        Project project = projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER, ProjectRole.EDITOR));
        Long userId = authUtils.getCurrentUserId();
        User currentUser = userRepository.getReferenceById(userId);

        ChatSession chatSession = resolveChatSession(project, currentUser, request.chatSessionId(), request.prompt());

        ChatMessage userMessage = chatMessageRepository.save(ChatMessage.builder()
                .chatSession(chatSession)
                .role(MessageRole.USER)
                .content(request.prompt())
                .toolCalls(null)
                .tokensUsed(0)
                .build());

        String rawResponse = chatClientBuilder.build()
                .prompt()
                .system(aiGenerationProperties.getGenerationSystemPrompt())
                .user(buildGenerationPrompt(project, request.prompt()))
                .call()
                .content();

        GeneratedProjectPayload payload = parsePayload(rawResponse);
        String summary = payload.summary() == null || payload.summary().isBlank()
                ? "Generated " + payload.files().size() + " files from the prompt."
                : payload.summary().trim();

        List<GeneratedFileResponse> changedFiles = payload.files().stream()
                .map(file -> upsertProjectFile(project, currentUser, file))
                .toList();

        ChatMessage assistantMessage = chatMessageRepository.save(ChatMessage.builder()
                .chatSession(chatSession)
                .role(MessageRole.ASSISTANT)
                .content(summary)
                .toolCalls("project_file_upsert")
                .tokensUsed(0)
                .build());

        if (payload.title() != null && !payload.title().isBlank()) {
            chatSession.setTitle(payload.title().trim());
        }
        chatSessionRepository.save(chatSession);

        usageLogRepository.save(UsageLog.builder()
                .user(currentUser)
                .project(project)
                .action("ai_project_generate")
                .tokenUsed(0)
                .durationMs(null)
                .metadata("{\"chatSessionId\":" + chatSession.getId() + ",\"generatedFiles\":" + changedFiles.size() + "}")
                .build());

        return new GenerateProjectResponse(
                chatSession.getId(),
                userMessage.getId(),
                assistantMessage.getId(),
                summary,
                changedFiles
        );
    }

    private ChatSession resolveChatSession(Project project, User currentUser, Long chatSessionId, String prompt) {
        if (chatSessionId != null) {
            return chatSessionRepository.findByIdAndProjectIdAndDeletedAtIsNull(chatSessionId, project.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ChatSession", chatSessionId.toString()));
        }

        return chatSessionRepository.save(ChatSession.builder()
                .project(project)
                .user(currentUser)
                .title(defaultTitle(prompt))
                .deletedAt(null)
                .build());
    }

    private String buildGenerationPrompt(Project project, String prompt) {
        StringBuilder builder = new StringBuilder();
        builder.append("Project name: ").append(project.getName()).append("\n");
        builder.append("Prompt: ").append(prompt).append("\n");
        builder.append("Return valid JSON only with this schema:\n");
        builder.append("{\"title\":\"short title\",\"summary\":\"what was generated\",\"files\":[{\"path\":\"relative/path\",\"content\":\"full file content\"}]}\n");
        builder.append("Generate a coherent starter project. Always return complete file contents.");
        return builder.toString();
    }

    private GeneratedProjectPayload parsePayload(String rawResponse) {
        String normalized = rawResponse == null ? "" : rawResponse.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```json\\s*", "");
            normalized = normalized.replaceFirst("^```\\s*", "");
            normalized = normalized.replaceFirst("\\s*```$", "");
        }

        try {
            GeneratedProjectPayload payload = objectMapper.readValue(normalized, GeneratedProjectPayload.class);
            if (payload.files() == null || payload.files().isEmpty()) {
                throw new BadRequestException("AI response did not contain files");
            }
            return payload;
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("AI response was not valid JSON");
        }
    }

    private GeneratedFileResponse upsertProjectFile(Project project, User currentUser, GeneratedFilePayload file) {
        String normalizedPath = normalizePath(file.path());
        ProjectFile existing = projectFileRepository.findByProjectIdAndPath(project.getId(), normalizedPath).orElse(null);
        String operation = existing == null ? "created" : "updated";

        ProjectFile projectFile = existing == null ? new ProjectFile() : existing;
        projectFile.setProject(project);
        projectFile.setPath(normalizedPath);
        projectFile.setMinioObjectKey(existing == null ? storageKey(project.getId(), normalizedPath) : existing.getMinioObjectKey());
        projectFile.setContent(file.content());
        if (existing == null) {
            projectFile.setCreatedBy(currentUser);
        }
        projectFile.setUpdatedBy(currentUser);

        projectFileRepository.save(projectFile);
        return new GeneratedFileResponse(normalizedPath, operation);
    }

    private String defaultTitle(String prompt) {
        String compact = prompt.trim().replaceAll("\\s+", " ");
        return compact.length() <= 60 ? compact : compact.substring(0, 60) + "...";
    }

    private String normalizePath(String path) {
        if (path == null) {
            throw new BadRequestException("Generated file path is required");
        }
        String normalized = path.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank() || normalized.contains("..")) {
            throw new BadRequestException("Generated file path is invalid");
        }
        return normalized;
    }

    private String storageKey(Long projectId, String path) {
        return "project-" + projectId + "/" + UUID.randomUUID() + "/" + path;
    }

    private record GeneratedProjectPayload(
            String title,
            String summary,
            List<GeneratedFilePayload> files
    ) {}

    private record GeneratedFilePayload(
            String path,
            String content
    ) {}
}

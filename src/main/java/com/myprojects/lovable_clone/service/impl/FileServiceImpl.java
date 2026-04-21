package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.project.CreateFileRequest;
import com.myprojects.lovable_clone.dto.project.FileContentResponse;
import com.myprojects.lovable_clone.dto.project.FileNode;
import com.myprojects.lovable_clone.dto.project.UpdateFileRequest;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectFile;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.exceptions.BadRequestException;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.ProjectFileRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.security.ProjectAuthorizationService;
import com.myprojects.lovable_clone.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {
    private final ProjectFileRepository projectFileRepository;
    private final ProjectAuthorizationService projectAuthorizationService;
    private final AuthUtils authUtils;
    private final UserRepository userRepository;

    @Override
    public List<FileNode> getFileTree(Long projectId) {
        projectAuthorizationService.getAccessibleProject(projectId);
        List<ProjectFile> files = projectFileRepository.findByProjectIdOrderByPathAsc(projectId);

        Map<String, FileNode> nodes = new LinkedHashMap<>();
        for (ProjectFile file : files) {
            String normalizedPath = normalizePath(file.getPath());
            addDirectoryNodes(nodes, normalizedPath, file.getUpdatedAt());
            nodes.put(
                    normalizedPath,
                    new FileNode(
                            normalizedPath,
                            file.getUpdatedAt(),
                            (long) file.getContent().getBytes(StandardCharsets.UTF_8).length,
                            "file"
                    )
            );
        }

        return nodes.values().stream()
                .sorted(Comparator.comparing(FileNode::path))
                .toList();
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        projectAuthorizationService.getAccessibleProject(projectId);
        String normalizedPath = normalizePath(path);
        ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, normalizedPath)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectFile", projectId + ":" + normalizedPath));
        return new FileContentResponse(file.getPath(), file.getContent());
    }

    @Override
    public FileContentResponse createFile(Long projectId, CreateFileRequest request) {
        Project project = projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER, ProjectRole.EDITOR));
        Long userId = authUtils.getCurrentUserId();
        User currentUser = userRepository.getReferenceById(userId);
        String normalizedPath = normalizePath(request.path());

        if (projectFileRepository.existsByProjectIdAndPath(projectId, normalizedPath)) {
            throw new BadRequestException("File already exists at path: " + normalizedPath);
        }

        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .path(normalizedPath)
                .minioObjectKey(generateStorageKey(projectId, normalizedPath))
                .content(request.content())
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        projectFile = projectFileRepository.save(projectFile);
        return new FileContentResponse(projectFile.getPath(), projectFile.getContent());
    }

    @Override
    public FileContentResponse updateFile(Long projectId, String path, UpdateFileRequest request) {
        projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER, ProjectRole.EDITOR));
        Long userId = authUtils.getCurrentUserId();
        User currentUser = userRepository.getReferenceById(userId);
        String normalizedPath = normalizePath(path);

        ProjectFile projectFile = projectFileRepository.findByProjectIdAndPath(projectId, normalizedPath)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectFile", projectId + ":" + normalizedPath));
        projectFile.setContent(request.content());
        projectFile.setUpdatedBy(currentUser);
        projectFile = projectFileRepository.save(projectFile);

        return new FileContentResponse(projectFile.getPath(), projectFile.getContent());
    }

    @Override
    public void deleteFile(Long projectId, String path) {
        projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER, ProjectRole.EDITOR));
        String normalizedPath = normalizePath(path);

        if (!projectFileRepository.existsByProjectIdAndPath(projectId, normalizedPath)) {
            throw new ResourceNotFoundException("ProjectFile", projectId + ":" + normalizedPath);
        }
        projectFileRepository.deleteByProjectIdAndPath(projectId, normalizedPath);
    }

    private void addDirectoryNodes(Map<String, FileNode> nodes, String filePath, java.time.Instant modifiedAt) {
        String[] parts = filePath.split("/");
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.isEmpty()) {
                current.append("/");
            }
            current.append(parts[i]);
            String directoryPath = current.toString();
            nodes.putIfAbsent(directoryPath, new FileNode(directoryPath, modifiedAt, 0L, "directory"));
        }
    }

    private String normalizePath(String path) {
        if (path == null) {
            throw new BadRequestException("File path is required");
        }

        String normalized = path.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isBlank()) {
            throw new BadRequestException("File path cannot be blank");
        }
        if (normalized.contains("..")) {
            throw new BadRequestException("File path cannot contain '..'");
        }
        return normalized;
    }

    private String generateStorageKey(Long projectId, String path) {
        return "project-" + projectId + "/" + UUID.randomUUID() + "/" + path;
    }
}

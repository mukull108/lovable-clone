package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.project.ProjectRequest;
import com.myprojects.lovable_clone.dto.project.ProjectResponse;
import com.myprojects.lovable_clone.dto.project.ProjectSummaryResponse;
import org.jspecify.annotations.Nullable;

import java.util.*;

public interface ProjectService {
    List<ProjectSummaryResponse> getProject(Long id);

    ProjectResponse getUserProjectById(Long id, Long userId);

    ProjectResponse createProject(ProjectRequest request, Long userId);

    ProjectResponse updateProject(Long id, ProjectRequest request, Long userId);

    void softDelete(Long id, Long userId);
}

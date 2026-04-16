package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.project.ProjectRequest;
import com.myprojects.lovable_clone.dto.project.ProjectResponse;
import com.myprojects.lovable_clone.dto.project.ProjectSummaryResponse;
import org.jspecify.annotations.Nullable;

import java.util.*;

public interface ProjectService {
    List<ProjectSummaryResponse> getProject();

    ProjectResponse getUserProjectById(Long projectId);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(Long projectId, ProjectRequest request);

    void softDelete(Long projectId);
}

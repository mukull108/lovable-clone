package com.myprojects.lovable_clone.security;

import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.exceptions.ForbiddenException;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import com.myprojects.lovable_clone.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectAuthorizationService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtils authUtils;

    public Project getAccessibleProject(Long projectId) {
        Long userId = authUtils.getCurrentUserId();
        return projectRepository.findAccessibleProjectByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));
    }

    public Project getProjectForRole(Long projectId, Set<ProjectRole> allowedRoles) {
        Long userId = authUtils.getCurrentUserId();
        Project project = projectRepository.findAccessibleProjectByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));

        ProjectMember projectMember = projectMemberRepository.findByIdProjectIdAndIdUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectMember", projectId + ":" + userId));

        if (!allowedRoles.contains(projectMember.getRole())) {
            throw new ForbiddenException("You do not have permission to perform this action on the project");
        }

        return project;
    }
}

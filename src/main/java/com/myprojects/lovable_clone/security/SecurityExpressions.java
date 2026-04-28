package com.myprojects.lovable_clone.security;

import com.myprojects.lovable_clone.enums.ProjectPermissions;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
public class SecurityExpressions {

    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtils authUtils;

    private Boolean hasPermission(Long projectId, ProjectPermissions permissions){
        Long userId = authUtils.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(permissions)
                ).orElse(false);
    }

    public Boolean canViewProject(Long projectId) {
        return hasPermission(projectId, ProjectPermissions.VIEW);

    }

    public Boolean canEditProject(Long projectId) {
        return hasPermission(projectId, ProjectPermissions.EDIT);
    }
    public Boolean canDeleteProject(Long projectId) {
        return hasPermission(projectId, ProjectPermissions.DELETE);
    }
}

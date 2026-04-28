package com.myprojects.lovable_clone.security;

import com.myprojects.lovable_clone.enums.Permissions;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
public class SecurityExpressions {

    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtils authUtils;

    private Boolean hasPermission(Long projectId, Permissions permissions){
        Long userId = authUtils.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(permissions)
                ).orElse(false);
    }

    public Boolean canViewProject(Long projectId) {
        return hasPermission(projectId, Permissions.VIEW);
    }

    public Boolean canEditProject(Long projectId) {
        return hasPermission(projectId, Permissions.EDIT);
    }
    public Boolean canDeleteProject(Long projectId) {
        return hasPermission(projectId, Permissions.DELETE);
    }

    public Boolean canViewMembers(Long projectId) {
        return hasPermission(projectId, Permissions.VIEW_MEMBERS);
    }

    public Boolean canManageMembers(Long projectId) {
        return hasPermission(projectId, Permissions.MANAGE_MEMBERS);
    }
}

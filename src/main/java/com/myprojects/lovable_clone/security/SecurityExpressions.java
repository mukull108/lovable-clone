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

    public Boolean canViewProject(Long projectId) {
        Long userId = authUtils.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role ->
                    role.getPermissions().contains(ProjectPermissions.VIEW)
                ).orElse(false);

    }

    public Boolean canEditProject(Long projectId) {
        Long userId = authUtils.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(ProjectPermissions.EDIT)
                ).orElse(false);
    }
}

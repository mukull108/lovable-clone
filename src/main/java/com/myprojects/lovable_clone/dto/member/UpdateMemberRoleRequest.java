package com.myprojects.lovable_clone.dto.member;

import com.myprojects.lovable_clone.enums.ProjectRole;

public record UpdateMemberRoleRequest(
        ProjectRole role
) {
}

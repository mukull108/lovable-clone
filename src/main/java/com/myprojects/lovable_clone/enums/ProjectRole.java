package com.myprojects.lovable_clone.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import static com.myprojects.lovable_clone.enums.ProjectPermissions.*;

@Getter
public enum ProjectRole {

    VIEWER(VIEW),
    EDITOR(EDIT, VIEW, DELETE),
    OWNER(VIEW, EDIT, DELETE, MANAGE_MEMBERS);

    ProjectRole(ProjectPermissions... permissions) {
        this.permissions = Set.of(permissions);
    }

    private final Set<ProjectPermissions> permissions;


}

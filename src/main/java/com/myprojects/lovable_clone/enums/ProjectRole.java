package com.myprojects.lovable_clone.enums;

import lombok.Getter;

import java.util.Set;
import static com.myprojects.lovable_clone.enums.Permissions.*;

@Getter
public enum ProjectRole {

    VIEWER(VIEW, VIEW_MEMBERS),
    EDITOR(EDIT, VIEW, DELETE, VIEW_MEMBERS),
    OWNER(VIEW, EDIT, DELETE, MANAGE_MEMBERS, VIEW_MEMBERS);

    ProjectRole(Permissions... permissions) {
        this.permissions = Set.of(permissions);
    }

    private final Set<Permissions> permissions;


}

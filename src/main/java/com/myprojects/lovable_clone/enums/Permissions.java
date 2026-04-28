package com.myprojects.lovable_clone.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permissions {
    VIEW("project:view"),
    EDIT("project:edit"),
    DELETE("project:delete"),

    MANAGE_MEMBERS("project_members:manage"),
    VIEW_MEMBERS("project_members:view"),


    ;


    private final String value;
}

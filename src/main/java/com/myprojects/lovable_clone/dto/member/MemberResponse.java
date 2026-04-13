package com.myprojects.lovable_clone.dto.member;

import com.myprojects.lovable_clone.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String name,
        String email,
        ProjectRole role,
        Instant invitedAt
) {
}

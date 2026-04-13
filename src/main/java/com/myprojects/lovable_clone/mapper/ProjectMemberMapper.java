package com.myprojects.lovable_clone.mapper;

import com.myprojects.lovable_clone.dto.member.MemberResponse;
import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {
    @Mapping(target="userId", source = "id")
    @Mapping(target="role", constant = "OWNER")
    MemberResponse toMemberResponse(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    MemberResponse toMemberResponse(ProjectMember projectMember);
}

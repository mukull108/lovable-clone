package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.member.InviteMemberRequest;
import com.myprojects.lovable_clone.dto.member.MemberResponse;
import com.myprojects.lovable_clone.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {

    List<MemberResponse> getProjectMembers(Long userId, Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId);

    MemberResponse changeMemberRole(Long projectId, Long userId, Long memberId, UpdateMemberRoleRequest updateMemberRoleRequest);

    void removeProjectMember(Long projectId, Long userId, Long memberId);
}

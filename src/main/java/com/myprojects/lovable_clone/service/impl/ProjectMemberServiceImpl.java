package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.member.InviteMemberRequest;
import com.myprojects.lovable_clone.dto.member.MemberResponse;
import com.myprojects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.myprojects.lovable_clone.service.ProjectMemberService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {
    @Override
    public List<MemberResponse> getProjectMembers(Long userId, Long projectId) {
        return List.of();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId) {
        return null;
    }

    @Override
    public MemberResponse changeMemberRole(Long projectId, Long userId, Long memberId, UpdateMemberRoleRequest updateMemberRoleRequest) {
        return null;
    }

    @Override
    public MemberResponse deleteProjectMember(Long projectId, Long userId, Long memberId, UpdateMemberRoleRequest updateMemberRoleRequest) {
        return null;
    }
}

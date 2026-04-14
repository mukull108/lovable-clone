package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.member.InviteMemberRequest;
import com.myprojects.lovable_clone.dto.member.MemberResponse;
import com.myprojects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.entity.ProjectMemberId;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.mapper.ProjectMemberMapper;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import com.myprojects.lovable_clone.repository.ProjectRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    ProjectMemberMapper projectMemberMapper;
    UserRepository userRepository;

    @Override
    public List<MemberResponse> getProjectMembers(Long userId, Long projectId) {
        Project project = getAccessibleProjectById(projectId, userId);
        List<MemberResponse> memberResponseList = new ArrayList<>();
        memberResponseList.add(projectMemberMapper.toMemberResponse(project.getOwner()));


        memberResponseList.addAll(projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toMemberResponse)
                .toList());

        return memberResponseList;
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only project owner can invite members");
        }

        User invitee = userRepository.findUserByEmail(request.email()).orElseThrow();
        if (invitee.getId().equals(userId)) {
            throw new RuntimeException("Owner cannot invite themselves");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());
        if (projectMemberRepository.existsById(projectMemberId)) {
            throw new RuntimeException("User is already a member of the project");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .invitedAt(Instant.now())
                .id(projectMemberId)
                .project(project)
                .user(invitee)
                .role(request.role())
                .build();

        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toMemberResponse(projectMember);
    }

    @Override
    public MemberResponse changeMemberRole(Long projectId, Long userId, Long memberId, UpdateMemberRoleRequest updateMemberRoleRequest) {
        Project project = getAccessibleProjectById(projectId, userId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only project owner can update member's role");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();

        projectMember.setRole(updateMemberRoleRequest.role());
        projectMemberRepository.save(projectMember);

        return projectMemberMapper.toMemberResponse(projectMember);
    }

    @Override
    public void removeProjectMember(Long projectId, Long userId, Long memberId) {
        Project project = getAccessibleProjectById(projectId, userId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only project owner can update member's role");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        if (!projectMemberRepository.existsById(projectMemberId)) {
            throw new RuntimeException("User is not a member of the project");
        }
        projectMemberRepository.deleteById(projectMemberId);
    }

    public Project getAccessibleProjectById(Long id, Long userId) {
        return projectRepository.findAccessibleProjectByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
    }
}

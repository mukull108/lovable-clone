package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.member.InviteMemberRequest;
import com.myprojects.lovable_clone.dto.member.MemberResponse;
import com.myprojects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.entity.ProjectMemberId;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.exceptions.BadRequestException;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.mapper.ProjectMemberMapper;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.ProjectAuthorizationService;
import com.myprojects.lovable_clone.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepository;
    ProjectMemberMapper projectMemberMapper;
    UserRepository userRepository;
    ProjectAuthorizationService projectAuthorizationService;

    @Override
    public List<MemberResponse> getProjectMembers(Long projectId) {
        projectAuthorizationService.getAccessibleProject(projectId);
        return projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toMemberResponse)
                .toList();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Project project = projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER));

        User invitee = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.username()));
        if (projectMemberRepository.findByIdProjectIdAndIdUserId(projectId, invitee.getId()).isPresent()) {
            throw new BadRequestException("User is already a member of the project");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());
        if (request.role() == ProjectRole.OWNER) {
            throw new BadRequestException("Only one owner is supported per project");
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
    public MemberResponse changeMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest updateMemberRoleRequest) {
        projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER));

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectMember", projectId + ":" + memberId));

        if (projectMember.getRole() == ProjectRole.OWNER || updateMemberRoleRequest.role() == ProjectRole.OWNER) {
            throw new BadRequestException("Owner role cannot be reassigned through this endpoint");
        }

        projectMember.setRole(updateMemberRoleRequest.role());
        projectMemberRepository.save(projectMember);

        return projectMemberMapper.toMemberResponse(projectMember);
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId) {
        projectAuthorizationService.getProjectForRole(projectId, Set.of(ProjectRole.OWNER));

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectMember", projectId + ":" + memberId));
        if (projectMember.getRole() == ProjectRole.OWNER) {
            throw new BadRequestException("Project owner cannot be removed");
        }
        projectMemberRepository.deleteById(projectMemberId);
    }
}

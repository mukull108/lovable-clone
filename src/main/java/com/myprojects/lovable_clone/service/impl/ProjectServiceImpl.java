package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.project.ProjectRequest;
import com.myprojects.lovable_clone.dto.project.ProjectResponse;
import com.myprojects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.entity.ProjectMemberId;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.mapper.ProjectMapper;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import com.myprojects.lovable_clone.repository.ProjectRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.security.ProjectAuthorizationService;
import com.myprojects.lovable_clone.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {
    UserRepository userRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtils authUtils;
    ProjectRepository projectRepository;
    ProjectAuthorizationService projectAuthorizationService;


    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        Long userId = authUtils.getCurrentUserId();
//        User owner = userRepository.findById(userId).orElseThrow(
//                () -> new ResourceNotFoundException("User", userId.toString()));
        User owner = userRepository.getReferenceById(userId); // only works in transactional context, otherwise it will throw EntityNotFoundException when accessed. But since this method is transactional, it's fine to use getReferenceById which is more efficient than findById in this case.
        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);
        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), owner.getId());
        ProjectMember projectOwner = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .user(owner)
                .role(ProjectRole.OWNER)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .build();

        projectMemberRepository.save(projectOwner);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getProject() {
        Long userId = authUtils.getCurrentUserId();

//        return projectRepository.findAllAccessibleByUserId(userId)
//                .stream()
//                .map(projectMapper::toProjectSummaryResponse)
//                .toList()
//                ;
        List<Project> allAccessibleByUserId = projectRepository.findAllAccessibleByUserId(userId);
        return projectMapper.toListOfProjectSummaryResponse(allAccessibleByUserId);
    }

    @Override
    public ProjectResponse getUserProjectById(Long id) {
        Project project = projectAuthorizationService.getAccessibleProject(id);
        return projectMapper.toProjectResponse(project);
    }
    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectAuthorizationService.getProjectForRole(id, java.util.Set.of(ProjectRole.OWNER, ProjectRole.EDITOR));
        project.setName(request.name());
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(updatedProject);
    }

    @Override
    public void softDelete(Long id) {
        Project project = projectAuthorizationService.getProjectForRole(id, java.util.Set.of(ProjectRole.OWNER));
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }
}

package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.project.ProjectRequest;
import com.myprojects.lovable_clone.dto.project.ProjectResponse;
import com.myprojects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.entity.ProjectMemberId;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.enums.ProjectRole;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.mapper.ProjectMapper;
import com.myprojects.lovable_clone.repository.ProjectMemberRepository;
import com.myprojects.lovable_clone.repository.ProjectRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
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
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", userId.toString()));
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
    public List<ProjectSummaryResponse> getProject(Long userId) {

//        return projectRepository.findAllAccessibleByUserId(userId)
//                .stream()
//                .map(projectMapper::toProjectSummaryResponse)
//                .toList()
//                ;
        List<Project> allAccessibleByUserId = projectRepository.findAllAccessibleByUserId(userId);
        return projectMapper.toListOfProjectSummaryResponse(allAccessibleByUserId);
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        Project project = getAccessibleProjectById(id,userId);
        return projectMapper.toProjectResponse(project);
    }
    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = getAccessibleProjectById(id,userId);
        project.setName(request.name());
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(updatedProject);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        Project project = getAccessibleProjectById(id,userId);

        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }
    public Project getAccessibleProjectById(Long id, Long userId) {
        return projectRepository.findAccessibleProjectByIdAndUserId(id,userId).orElseThrow(() -> new ResourceNotFoundException("Project", id.toString()));
    }
}

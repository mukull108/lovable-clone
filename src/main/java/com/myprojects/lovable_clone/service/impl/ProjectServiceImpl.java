package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.project.ProjectRequest;
import com.myprojects.lovable_clone.dto.project.ProjectResponse;
import com.myprojects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.myprojects.lovable_clone.entity.Project;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.mapper.ProjectMapper;
import com.myprojects.lovable_clone.repository.ProjectRepository;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Project project = Project.builder()
                .name(request.name())
                .owner(owner)
                .isPublic(false)
                .build();

        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectResponse(savedProject);
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
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        return projectMapper.toProjectResponse(project);
    }
    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        return null;
    }

    @Override
    public ProjectResponse softDelete(Long id, Long userId) {
        return null;
    }
}

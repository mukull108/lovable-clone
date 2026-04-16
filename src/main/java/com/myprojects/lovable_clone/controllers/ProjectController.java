package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.project.ProjectRequest;
import com.myprojects.lovable_clone.dto.project.ProjectResponse;
import com.myprojects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.security.JwtUserPrincipal;
import com.myprojects.lovable_clone.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/projects")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ProjectController {

    ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>> getProject(){
        return ResponseEntity.ok(projectService.getProject());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId){
        return ResponseEntity.ok(projectService.getUserProjectById(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@RequestBody @Valid ProjectRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId, @RequestBody @Valid ProjectRequest request ){
        return ResponseEntity.ok(projectService.updateProject(projectId,request));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId){
        projectService.softDelete(projectId);
        return ResponseEntity.noContent().build();
    }
}

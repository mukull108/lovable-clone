package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.chat.GenerateProjectRequest;
import com.myprojects.lovable_clone.dto.chat.GenerateProjectResponse;
import com.myprojects.lovable_clone.service.GenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}")
public class GenerationController {
    private final GenerationService generationService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateProjectResponse> generateProject(
            @PathVariable Long projectId,
            @RequestBody @Valid GenerateProjectRequest request
    ) {
        return ResponseEntity.ok(generationService.generateProject(projectId, request));
    }
}

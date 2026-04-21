package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.chat.GenerateProjectRequest;
import com.myprojects.lovable_clone.dto.chat.GenerateProjectResponse;

public interface GenerationService {
    GenerateProjectResponse generateProject(Long projectId, GenerateProjectRequest request);
}

package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.project.FileContentResponse;
import com.myprojects.lovable_clone.dto.project.FileNode;
import com.myprojects.lovable_clone.service.FileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public List<FileNode> getFileTree(Long projectId) {
        return List.of();
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        return null;
    }
}

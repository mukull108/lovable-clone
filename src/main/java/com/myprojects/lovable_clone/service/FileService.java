package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.project.FileContentResponse;
import com.myprojects.lovable_clone.dto.project.FileNode;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface FileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);
}

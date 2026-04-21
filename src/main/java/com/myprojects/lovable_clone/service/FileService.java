package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.project.FileContentResponse;
import com.myprojects.lovable_clone.dto.project.FileNode;
import com.myprojects.lovable_clone.dto.project.CreateFileRequest;
import com.myprojects.lovable_clone.dto.project.UpdateFileRequest;

import java.util.List;

public interface FileService {
    List<FileNode> getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    FileContentResponse createFile(Long projectId, CreateFileRequest request);

    FileContentResponse updateFile(Long projectId, String path, UpdateFileRequest request);

    void deleteFile(Long projectId, String path);
}

package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.project.FileContentResponse;
import com.myprojects.lovable_clone.dto.project.FileNode;
import com.myprojects.lovable_clone.dto.project.CreateFileRequest;
import com.myprojects.lovable_clone.dto.project.UpdateFileRequest;
import com.myprojects.lovable_clone.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/projects/{projectId}/files")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId){
        return ResponseEntity.ok(fileService.getFileTree(projectId));
    }

    @GetMapping("/{*path}") // /src/hooks/AppHook.jsx
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable Long projectId,
            @PathVariable("path") String path
    ){
        return ResponseEntity.ok(fileService.getFileContent(projectId,path));
    }

    @PostMapping
    public ResponseEntity<FileContentResponse> createFile(
            @PathVariable Long projectId,
            @RequestBody @Valid CreateFileRequest request
    ) {
        return ResponseEntity.ok(fileService.createFile(projectId, request));
    }

    @PutMapping("/{*path}")
    public ResponseEntity<FileContentResponse> updateFile(
            @PathVariable Long projectId,
            @PathVariable("path") String path,
            @RequestBody @Valid UpdateFileRequest request
    ) {
        return ResponseEntity.ok(fileService.updateFile(projectId, path, request));
    }

    @DeleteMapping("/{*path}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long projectId,
            @PathVariable("path") String path
    ) {
        fileService.deleteFile(projectId, path);
        return ResponseEntity.noContent().build();
    }

}

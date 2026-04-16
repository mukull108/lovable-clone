package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.project.FileContentResponse;
import com.myprojects.lovable_clone.dto.project.FileNode;
import com.myprojects.lovable_clone.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
            @PathVariable String path
    ){
        return ResponseEntity.ok(fileService.getFileContent(projectId,path));
    }

}

package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.member.InviteMemberRequest;
import com.myprojects.lovable_clone.dto.member.MemberResponse;
import com.myprojects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getProjectMembers(@PathVariable Long projectId){
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));

    }

    @PostMapping
    public ResponseEntity<MemberResponse> inviteMember(
            @PathVariable Long projectId,
            @RequestBody @Valid InviteMemberRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMemberService.inviteMember(projectId,request));
    }

    @PatchMapping(path = "/{memberId}")
    public ResponseEntity<MemberResponse> changeMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestBody @Valid UpdateMemberRoleRequest updateMemberRoleRequest){
        return ResponseEntity.ok(projectMemberService.changeMemberRole(projectId,memberId, updateMemberRoleRequest));
    }

    @DeleteMapping(path = "/{memberId}")
    public ResponseEntity<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId){
        projectMemberService.removeProjectMember(projectId,memberId);
        return ResponseEntity.noContent().build();
    }


}

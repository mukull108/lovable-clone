package com.myprojects.lovable_clone.repository;

import com.myprojects.lovable_clone.entity.ProjectMember;
import com.myprojects.lovable_clone.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId);

}

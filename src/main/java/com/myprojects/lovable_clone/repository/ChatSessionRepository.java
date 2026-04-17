package com.myprojects.lovable_clone.repository;

import com.myprojects.lovable_clone.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByProjectIdAndDeletedAtIsNullOrderByUpdatedAtDesc(Long projectId);
}

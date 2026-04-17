package com.myprojects.lovable_clone.repository;

import com.myprojects.lovable_clone.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    @Query("""
            SELECT COALESCE(SUM(ul.tokenUsed), 0)
            FROM UsageLog ul
            WHERE ul.user.id = :userId
              AND ul.createdAt >= :from
              AND ul.createdAt < :to
            """)
    Long sumTokenUsedForUserBetween(@Param("userId") Long userId,
                                    @Param("from") Instant from,
                                    @Param("to") Instant to);
}

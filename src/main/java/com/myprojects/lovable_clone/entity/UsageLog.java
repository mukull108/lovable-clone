package com.myprojects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "usage_logs", indexes = {
        @Index(name = "idx_usage_logs_user_created_at", columnList = "user_id,created_at"),
        @Index(name = "idx_usage_logs_project_created_at", columnList = "project_id,created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    Project project;

    @Column(nullable = false, length = 100)
    String action;

    @Column(nullable = false)
    Integer tokenUsed = 0;

    Integer durationMs;

    @Lob
    String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;
}

package com.myprojects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "projects")
@Builder
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String name;

    @ManyToOne
    @JoinColumn(name = "owner_id",nullable = false)
    User owner;
    Boolean isPublic=false;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;
    Instant deletedAt; //soft delete
}

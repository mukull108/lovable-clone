package com.myprojects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

//@Entity
//@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class User {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @Column(unique = true)
    String email;

    String name;

    String password_hash;

    String avatar_url;

//    @CreationTimestamp
    Instant createdAt;

//    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt; //soft delete


}

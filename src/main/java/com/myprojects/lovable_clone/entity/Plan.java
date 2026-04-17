package com.myprojects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "plans", indexes = {
        @Index(name = "idx_plans_active", columnList = "active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 100)
    String name;

    @Column(unique = true)
    String stripePriceId;

    @Column(nullable = false)
    Integer maxProjects;

    @Column(nullable = false)
    Integer maxTokensPerDay;

    @Column(nullable = false)
    Integer maxPreviews;

    @Column(nullable = false)
    Boolean unlimitedAi = false;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @Column(nullable = false)
    Boolean active = true;
}

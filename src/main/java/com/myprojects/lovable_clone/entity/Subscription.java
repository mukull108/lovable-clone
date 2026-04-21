package com.myprojects.lovable_clone.entity;

import com.myprojects.lovable_clone.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_user_status", columnList = "user_id,status"),
        @Index(name = "idx_subscriptions_period_end", columnList = "current_period_end")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    Plan plan;

    @Column
    String stripeCustomerId;

    @Column(unique = true)
    String stripeSubscriptionId;

    @Column(nullable = false)
    Instant currentPeriodStart;

    @Column(nullable = false)
    Instant currentPeriodEnd;

    @Column(nullable = false)
    Boolean cancelAtPeriodEnd = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    SubscriptionStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    Instant updatedAt;
}

package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType type;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    @PrePersist
    public void prePersist() {
        this.earnedAt = LocalDateTime.now();
    }

    public enum BadgeType {
        // Streak badges
        STREAK_3,
        STREAK_7,
        STREAK_30,
        STREAK_100,

        // XP / Level badges
        LEVEL_5,
        LEVEL_10,
        LEVEL_20,

        // Activity badges
        FIRST_HABIT,
        FIRST_FOCUS,
        FIRST_JOURNAL,
        FOCUS_MASTER,       // 60 min focus in one day
        JOURNAL_STREAK_7    // 7 journal entries
    }
}
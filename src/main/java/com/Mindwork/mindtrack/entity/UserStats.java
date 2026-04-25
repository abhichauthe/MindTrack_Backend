package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalXp = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalHabitsCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalFocusMinutes = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalJournalEntries = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Level up every 100 XP
    public void addXp(int xp) {
        this.totalXp += xp;
        this.level = (this.totalXp / 100) + 1;
    }
}
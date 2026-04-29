package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "recurring_habits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringHabit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Core info ─────────────────────────────────────────────────
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockCategory category;

    // ── Schedule ──────────────────────────────────────────────────
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // DAILY only as per requirement
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    @Builder.Default
    private RepeatType repeatType = RepeatType.DAILY;

    // ── Status ────────────────────────────────────────────────────
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // When this recurring habit starts generating blocks
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // When to stop (null = forever)
    @Column(name = "end_date")
    private LocalDate endDate;

    // ── Timestamps ────────────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.startDate == null) this.startDate = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum RepeatType { DAILY }

    public enum BlockCategory { HABIT, DISCIPLINE, WORK, PERSONAL }
}
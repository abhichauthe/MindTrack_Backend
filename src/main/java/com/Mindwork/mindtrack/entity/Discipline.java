package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disciplines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Area info ─────────────────────────────────────────────────────
    @Column(name = "area_name", nullable = false)
    private String areaName;

    @Column(name = "area_id")
    private String areaId;

    @Column(name = "area_emoji")
    private String areaEmoji;

    // ── Discipline definition ─────────────────────────────────────────
    @Column(nullable = false, columnDefinition = "TEXT")
    private String behavior;

    @Column(name = "daily_action", columnDefinition = "TEXT")
    private String dailyAction;

    // ── Schedule ──────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;

    // Comma-separated days e.g. "MON,WED,FRI" — only used when scheduleType = SPECIFIC
    @Column(name = "schedule_days")
    private String scheduleDays;

    // ── Status ────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisciplineStatus status;

    // ── Timestamps ────────────────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // In Discipline.java entity — add these two fields
    @Column(name = "framework")
    private String framework;

    @Column(name = "weekly_plan", columnDefinition = "TEXT")
    private String weeklyPlan;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DisciplineStatus.ACTIVE;
        }
        if (this.scheduleType == null) {
            this.scheduleType = ScheduleType.DAILY;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Enums ─────────────────────────────────────────────────────────

    public enum ScheduleType {
        DAILY,      // Every day
        SPECIFIC,   // Specific days of week e.g. MON, WED, FRI
        FLEXIBLE    // No fixed schedule — user decides
    }

    public enum DisciplineStatus {
        ACTIVE,    // Currently tracking
        PAUSED,    // Temporarily paused
        COMPLETED, // Goal achieved
        ARCHIVED   // No longer tracking
    }
}
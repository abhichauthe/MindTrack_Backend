package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "daily_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Relations ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Optional link to the weekly breakdown this task belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_breakdown_id")
    private WeeklyBreakdown weeklyBreakdown;

    /**
     * Optional back-reference to the timetable block created for this task.
     * Set by TaskSyncService after syncing.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_block_id")
    private TimeBlock timetableBlock;

    // ─── Core Task Info ───────────────────────────────────────────────

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskCategory category;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    // ─── Scheduling ───────────────────────────────────────────────────

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(name = "due_time")
    private LocalTime dueTime;

    /** Duration of the task in minutes (used for conflict detection). */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Actual time spent on the task in minutes.
     * Used by ProductivityAnalyticsService for planned vs actual comparison.
     */
    @Column(name = "actual_minutes_spent")
    private Integer actualMinutesSpent;

    /**
     * The exact datetime this task was scheduled at (set during timetable sync).
     * Used by TaskSyncService.
     */
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    // ─── Snooze ───────────────────────────────────────────────────────

    /**
     * How many times this task has been snoozed.
     * Used by ProductivityAnalyticsService for snooze behavior analytics.
     */
    @Builder.Default
    @Column(name = "snooze_count", nullable = false)
    private Integer snoozeCount = 0;

    // ─── Timetable Sync ───────────────────────────────────────────────

    @Builder.Default
    @Column(name = "synced_to_timetable", nullable = false)
    private boolean syncedToTimetable = false;

    // ─── Notification Flags ───────────────────────────────────────────
    //
    // Boxed Boolean (not primitive) so Lombok generates getPreReminderSent()
    // instead of isPreReminderSent(). The scheduler uses Boolean.TRUE.equals(...)
    // which handles null safely.

    @Builder.Default
    @Column(name = "pre_reminder_sent", nullable = false)
    private Boolean preReminderSent = false;

    @Builder.Default
    @Column(name = "start_notif_sent", nullable = false)
    private Boolean startNotifSent = false;

    @Builder.Default
    @Column(name = "missed_notif_sent", nullable = false)
    private Boolean missedNotifSent = false;

    // ─── Timestamps ───────────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ─── Lifecycle ────────────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TaskStatus.PENDING;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }
        if (this.snoozeCount == null) {
            this.snoozeCount = 0;
        }
    }

    // ─── Enums ────────────────────────────────────────────────────────

    public enum TaskStatus {
        PENDING, IN_PROGRESS, DONE, SKIPPED
    }

    /**
     * DISCIPLINE added to support TaskSyncService.mapCategory().
     * All original values preserved.
     */
    public enum TaskCategory {
        WORK, STUDY, HEALTH, PERSONAL, DISCIPLINE, OTHER
    }

    public enum TaskPriority {
        HIGH, MEDIUM, LOW
    }
}
package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // The date this block belongs to
    @Column(nullable = false)
    private LocalDate date;

    // Start and end time of the block
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // Category determines color coding on frontend
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockCategory category = BlockCategory.PERSONAL;

    // Completion status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockStatus status = BlockStatus.PENDING;

    // Optional link to a habit (if pulled from habits module)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id")
    private Habit habit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private DailyTask task;

    @Column(name = "auto_scheduled")
    @Builder.Default
    private boolean autoScheduled = false;

    // Link to the recurring habit template that created this block
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_habit_id")
    private RecurringHabit recurringHabit;

    // Whether this block was auto-generated from a recurring habit
    @Column(name = "is_recurring")
    @Builder.Default
    private boolean recurring = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum BlockCategory {
        HABIT,
        DISCIPLINE,
        WORK,
        PERSONAL
    }

    public enum BlockStatus {
        PENDING,
        IN_PROGRESS,
        DONE,
        SKIPPED
    }
}
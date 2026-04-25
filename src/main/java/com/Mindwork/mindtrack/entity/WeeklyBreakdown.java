package com.Mindwork.mindtrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_breakdowns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_plan_id", nullable = false)
    private MonthlyPlan monthlyPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer weekNumber;   // 1–4

    @Column(nullable = false)
    private String theme;         // e.g. "Foundation", "Build", "Stretch", "Review"

    @Column(columnDefinition = "TEXT")
    private String focus;         // What to focus on this week

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }
}
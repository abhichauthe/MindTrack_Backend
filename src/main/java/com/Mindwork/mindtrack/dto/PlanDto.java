package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.DailyTask;
import com.Mindwork.mindtrack.entity.MonthlyPlan;
import com.Mindwork.mindtrack.entity.WeeklyBreakdown;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class PlanDto {

    // ── Monthly Plan ──────────────────────────────────────────────────
    @Data
    public static class CreatePlanRequest {
        private String title;
        private String description;
        private Integer month;
        private Integer year;
        private Long disciplineId;     // optional link
        private boolean autoGenerate;  // auto-generate weeks + tasks
    }

    @Data
    public static class MonthlyPlanResponse {
        private Long id;
        private String title;
        private String description;
        private Integer month;
        private Integer year;
        private String monthName;
        private MonthlyPlan.PlanStatus status;
        private Long disciplineId;
        private LocalDateTime createdAt;
        private List<WeeklyBreakdownResponse> weeks;

        public MonthlyPlanResponse(MonthlyPlan p) {
            this.id           = p.getId();
            this.title        = p.getTitle();
            this.description  = p.getDescription();
            this.month        = p.getMonth();
            this.year         = p.getYear();
            this.monthName    = java.time.Month.of(p.getMonth()).name();
            this.status       = p.getStatus();
            this.disciplineId = p.getDiscipline() != null ? p.getDiscipline().getId() : null;
            this.createdAt    = p.getCreatedAt();
        }
    }

    // ── Weekly Breakdown ──────────────────────────────────────────────
    @Data
    public static class WeeklyBreakdownResponse {
        private Long id;
        private Integer weekNumber;
        private String theme;
        private String focus;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<DailyTaskResponse> tasks;
        private int totalTasks;
        private int completedTasks;

        public WeeklyBreakdownResponse(WeeklyBreakdown w) {
            this.id         = w.getId();
            this.weekNumber = w.getWeekNumber();
            this.theme      = w.getTheme();
            this.focus      = w.getFocus();
            this.startDate  = w.getStartDate();
            this.endDate    = w.getEndDate();
        }
    }

    // ── Daily Task ────────────────────────────────────────────────────
    @Data
    public static class CreateTaskRequest {
        private Long weeklyBreakdownId;   // optional
        private String title;
        private String description;
        private DailyTask.TaskCategory category;
        private DailyTask.TaskPriority priority;
        private LocalDate dueDate;
        private LocalTime dueTime;
        private Integer durationMinutes;
        private boolean syncToTimetable;
    }

    @Data
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private DailyTask.TaskCategory category;
        private DailyTask.TaskPriority priority;
        private LocalDate dueDate;
        private LocalTime dueTime;
        private Integer durationMinutes;
        private DailyTask.TaskStatus status;
    }

    @Data
    public static class DailyTaskResponse {
        private Long id;
        private Long weeklyBreakdownId;
        private String title;
        private String description;
        private DailyTask.TaskCategory category;
        private DailyTask.TaskPriority priority;
        private DailyTask.TaskStatus status;
        private LocalDate dueDate;
        private LocalTime dueTime;
        private Integer durationMinutes;
        private boolean syncedToTimetable;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public DailyTaskResponse(DailyTask t) {
            this.id                  = t.getId();
            this.weeklyBreakdownId   = t.getWeeklyBreakdown() != null ? t.getWeeklyBreakdown().getId() : null;
            this.title               = t.getTitle();
            this.description         = t.getDescription();
            this.category            = t.getCategory();
            this.priority            = t.getPriority();
            this.status              = t.getStatus();
            this.dueDate             = t.getDueDate();
            this.dueTime             = t.getDueTime();
            this.durationMinutes     = t.getDurationMinutes();
            this.syncedToTimetable   = t.isSyncedToTimetable();
            this.createdAt           = t.getCreatedAt();
            this.completedAt         = t.getCompletedAt();
        }
    }

    // ── Today summary ─────────────────────────────────────────────────
    @Data
    public static class TodaySummary {
        private LocalDate date;
        private long totalTasks;
        private long completedTasks;
        private int completionPercent;
        private List<DailyTaskResponse> tasks;

        public TodaySummary(LocalDate date, long total, long completed, List<DailyTaskResponse> tasks) {
            this.date              = date;
            this.totalTasks        = total;
            this.completedTasks    = completed;
            this.completionPercent = total > 0 ? (int) ((completed * 100) / total) : 0;
            this.tasks             = tasks;
        }
    }
}
package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.Discipline;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class DisciplineDto {

    // ── Create Request (from wizard completion) ───────────────────────
    @Data
    public static class CreateRequest {
        private String areaName;
        private String areaId;
        private String areaEmoji;
        private String behavior;
        private String dailyAction;
        private String scheduleType;  // DAILY, SPECIFIC, FLEXIBLE
        private List<String> days;    // e.g. ["MON","WED","FRI"]
        private String framework;     // e.g. "50/30/20 Rule"
        private String weeklyPlan;    // Stored as JSON or delimited string
    }

    // ── Update Request ────────────────────────────────────────────────
    @Data
    public static class UpdateRequest {
        private String behavior;
        private String dailyAction;
        private String scheduleType;
        private List<String> days;
        private String status;
    }

    // ── Response ──────────────────────────────────────────────────────
    @Data
    public static class DisciplineResponse {

        private Long id;
        private String areaName;
        private String areaId;
        private String areaEmoji;
        private String behavior;
        private String dailyAction;
        private Discipline.ScheduleType scheduleType;
        private List<String> scheduleDays;
        private Discipline.DisciplineStatus status;
        private String framework;
        private List<String> weeklyPlan;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DisciplineResponse(Discipline d) {
            this.id = d.getId();
            this.areaName = d.getAreaName();
            this.areaId = d.getAreaId();
            this.areaEmoji = d.getAreaEmoji();
            this.behavior = d.getBehavior();
            this.dailyAction = d.getDailyAction();
            this.scheduleType = d.getScheduleType();
            this.status = d.getStatus();
            this.createdAt = d.getCreatedAt();
            this.updatedAt = d.getUpdatedAt();

            // Convert comma-separated string → List
            this.scheduleDays = d.getScheduleDays() != null && !d.getScheduleDays().isEmpty()
                    ? Arrays.asList(d.getScheduleDays().split(","))
                    : List.of();

            // Framework
            this.framework = d.getFramework();

            // Convert weeklyPlan string → List
            this.weeklyPlan = d.getWeeklyPlan() != null && !d.getWeeklyPlan().isEmpty()
                    ? Arrays.asList(d.getWeeklyPlan().split("\\|"))
                    : List.of();
        }
    }
}

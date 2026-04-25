package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.TimeBlock;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class TimeBlockDto {

    // ── Create Request ────────────────────────────────────────────────────
    @Data
    public static class CreateRequest {
        private String title;
        private String description;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private TimeBlock.BlockCategory category;
        private Long habitId; // optional — links block to a habit
    }

    // ── Update Request ────────────────────────────────────────────────────
    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private TimeBlock.BlockCategory category;
        private TimeBlock.BlockStatus status;
    }

    // ── Status Update (mark done/skip/etc.) ───────────────────────────────
    @Data
    public static class StatusUpdateRequest {
        private TimeBlock.BlockStatus status;
    }

    // ── Full Response ─────────────────────────────────────────────────────
    @Data
    public static class TimeBlockResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private TimeBlock.BlockCategory category;
        private TimeBlock.BlockStatus status;
        private Long habitId;
        private String habitName;
        private int durationMinutes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TimeBlockResponse(TimeBlock b) {
            this.id              = b.getId();
            this.title           = b.getTitle();
            this.description     = b.getDescription();
            this.date            = b.getDate();
            this.startTime       = b.getStartTime();
            this.endTime         = b.getEndTime();
            this.category        = b.getCategory();
            this.status          = b.getStatus();
            this.habitId         = b.getHabit() != null ? b.getHabit().getId() : null;
            this.habitName       = b.getHabit() != null ? b.getHabit().getName() : null;
            this.durationMinutes = (int) java.time.Duration.between(
                    b.getStartTime(), b.getEndTime()).toMinutes();
            this.createdAt       = b.getCreatedAt();
            this.updatedAt       = b.getUpdatedAt();
        }
    }

    // ── Daily Summary ─────────────────────────────────────────────────────
    @Data
    public static class DailySummaryResponse {
        private LocalDate date;
        private long totalBlocks;
        private long completedBlocks;
        private long pendingBlocks;
        private int completionPercent;
        private java.util.List<TimeBlockResponse> blocks;

        public DailySummaryResponse(LocalDate date, long total, long completed,
                                    java.util.List<TimeBlockResponse> blocks) {
            this.date              = date;
            this.totalBlocks       = total;
            this.completedBlocks   = completed;
            this.pendingBlocks     = total - completed;
            this.completionPercent = total > 0
                    ? (int) ((completed * 100) / total)
                    : 0;
            this.blocks            = blocks;
        }
    }
}
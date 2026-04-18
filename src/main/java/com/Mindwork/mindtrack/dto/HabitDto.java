package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.HabitLog;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HabitDto {

    @Data
    public static class CreateHabitRequest {
        private String name;
        private String description;
    }

    @Data
    public static class HabitResponse {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        private boolean completedToday;

        public HabitResponse(Long id, String name, String description,
                             LocalDateTime createdAt, boolean completedToday) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.createdAt = createdAt;
            this.completedToday = completedToday;
        }
    }

    @Data
    public static class LogHabitRequest {
        private LocalDate date;
        private HabitLog.LogStatus status;
    }

    @Data
    public static class HabitLogResponse {
        private Long id;
        private Long habitId;
        private LocalDate date;
        private HabitLog.LogStatus status;
        private LocalDateTime loggedAt;

        public HabitLogResponse(Long id, Long habitId, LocalDate date,
                                HabitLog.LogStatus status, LocalDateTime loggedAt) {
            this.id = id;
            this.habitId = habitId;
            this.date = date;
            this.status = status;
            this.loggedAt = loggedAt;
        }
    }
}
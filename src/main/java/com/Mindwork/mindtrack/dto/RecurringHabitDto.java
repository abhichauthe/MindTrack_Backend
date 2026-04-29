package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.RecurringHabit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RecurringHabitDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "title is required")
        private String title;
        private String description;
        private RecurringHabit.BlockCategory category;
        @NotNull(message = "startTime is required")
        private LocalTime startTime;
        @NotNull(message = "endTime is required")
        private LocalTime endTime;
        private LocalDate startDate;   // optional — defaults to today
        private LocalDate endDate;     // optional — null = forever
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private RecurringHabit.BlockCategory category;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate endDate;
        private Boolean active;
    }

    @Data
    public static class RecurringHabitResponse {
        private Long id;
        private String title;
        private String description;
        private RecurringHabit.BlockCategory category;
        private LocalTime startTime;
        private LocalTime endTime;
        private RecurringHabit.RepeatType repeatType;
        private boolean active;
        private LocalDate startDate;
        private LocalDate endDate;
        private int durationMinutes;
        private LocalDateTime createdAt;

        public RecurringHabitResponse(RecurringHabit r) {
            this.id              = r.getId();
            this.title           = r.getTitle();
            this.description     = r.getDescription();
            this.category        = r.getCategory();
            this.startTime       = r.getStartTime();
            this.endTime         = r.getEndTime();
            this.repeatType      = r.getRepeatType();
            this.active          = r.isActive();
            this.startDate       = r.getStartDate();
            this.endDate         = r.getEndDate();
            this.createdAt       = r.getCreatedAt();
            this.durationMinutes = (int) java.time.Duration
                    .between(r.getStartTime(), r.getEndTime()).toMinutes();
        }
    }
}
package com.Mindwork.mindtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductivityReport {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private int totalTasks;
        private int completedTasks;
        private int missedTasks;
        private int completionRate;
        private int plannedMinutes;
        private int actualMinutes;
        private int mostProductiveHour;
        private int totalSnoozes;
        private List<DayStats> dailyStats;
        private Map<String, Long> completionsByCategory;
        private String insight;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayStats {
        private LocalDate date;
        private int totalTasks;
        private int completedTasks;

        public int getCompletionPercent() {
            return totalTasks > 0
                    ? (completedTasks * 100) / totalTasks : 0;
        }
    }
}
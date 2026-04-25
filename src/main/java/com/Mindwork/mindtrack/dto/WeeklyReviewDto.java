package com.Mindwork.mindtrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class WeeklyReviewDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyReport {
        // User info
        private String username;
        private String email;

        // Date range
        private LocalDate weekStart;
        private LocalDate weekEnd;

        // Habit stats
        private int totalHabits;
        private int habitsCompletedThisWeek;
        private int habitCompletionPercent;

        // Focus stats
        private int focusMinutesThisWeek;
        private int focusSessionsThisWeek;

        // Journal stats
        private int journalEntriesThisWeek;
        private String dominantMood;

        // Gamification
        private int xpEarnedThisWeek;
        private int currentLevel;
        private int bestStreakThisWeek;

        // Top habit (most completed)
        private String topHabitName;
        private int topHabitStreak;

        // Motivational message based on performance
        private String motivationalMessage;
        private String performanceGrade;  // S, A, B, C
    }
}
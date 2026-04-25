package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.WeeklyReviewDto;
import com.Mindwork.mindtrack.entity.*;
import com.Mindwork.mindtrack.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReviewService {

    private final UserRepository         userRepository;
    private final HabitRepository        habitRepository;
    private final HabitLogRepository     habitLogRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final UserStatsRepository    userStatsRepository;
    private final StreakRepository       streakRepository;

    public WeeklyReviewDto.WeeklyReport buildReportForUser(User user) {
        LocalDate weekEnd   = LocalDate.now();
        LocalDate weekStart = weekEnd.minusDays(6);

        // ── Habit Stats ───────────────────────────────────────────────
        List<Habit> habits   = habitRepository.findByUserId(user.getId());
        int totalHabits      = habits.size();
        int habitsCompleted  = countHabitsCompletedInRange(
                user.getId(), weekStart, weekEnd
        );
        int completionPercent = totalHabits > 0
                ? (habitsCompleted * 100) / (totalHabits * 7)
                : 0;
        completionPercent = Math.min(completionPercent, 100);

        // ── Focus Stats ───────────────────────────────────────────────
        List<FocusSession> focusSessions = focusSessionRepository
                .findByUserIdAndStatus(user.getId(), FocusSession.SessionStatus.COMPLETED)
                .stream()
                .filter(s -> s.getCompletedAt() != null
                        && !s.getCompletedAt().toLocalDate().isBefore(weekStart)
                        && !s.getCompletedAt().toLocalDate().isAfter(weekEnd))
                .toList();

        int focusMinutes  = focusSessions.stream()
                .mapToInt(FocusSession::getDurationMinutes).sum();
        int focusSessions7 = focusSessions.size();

        // ── Journal Stats ─────────────────────────────────────────────
        List<JournalEntry> journalEntries = journalEntryRepository
                .findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .filter(e -> !e.getDate().isBefore(weekStart)
                        && !e.getDate().isAfter(weekEnd))
                .toList();

        int journalCount  = journalEntries.size();
        String dominantMood = getDominantMood(journalEntries);

        // ── Gamification Stats ────────────────────────────────────────
        UserStats stats = userStatsRepository
                .findByUserId(user.getId())
                .orElse(null);

        int currentLevel = stats != null ? stats.getLevel() : 1;

        // Best streak this week
        int bestStreak = streakRepository
                .findByUserIdOrderByCurrentStreakDesc(user.getId())
                .stream()
                .mapToInt(Streak::getCurrentStreak)
                .max()
                .orElse(0);

        // Top habit by streak
        String topHabitName   = "N/A";
        int    topHabitStreak = 0;

        List<Streak> streaks = streakRepository
                .findByUserIdOrderByCurrentStreakDesc(user.getId());
        if (!streaks.isEmpty()) {
            Streak top    = streaks.get(0);
            topHabitName  = top.getHabit().getName();
            topHabitStreak= top.getCurrentStreak();
        }

        // XP earned this week (estimate from actions)
        int xpEarned = (habitsCompleted * 10)
                + (focusSessions7 * 15)
                + (journalCount * 10);

        // ── Performance Grade ─────────────────────────────────────────
        String grade = calculateGrade(completionPercent, focusMinutes, journalCount);
        String message = getMotivationalMessage(grade, user.getUsername());

        return WeeklyReviewDto.WeeklyReport.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .totalHabits(totalHabits)
                .habitsCompletedThisWeek(habitsCompleted)
                .habitCompletionPercent(completionPercent)
                .focusMinutesThisWeek(focusMinutes)
                .focusSessionsThisWeek(focusSessions7)
                .journalEntriesThisWeek(journalCount)
                .dominantMood(dominantMood)
                .xpEarnedThisWeek(xpEarned)
                .currentLevel(currentLevel)
                .bestStreakThisWeek(bestStreak)
                .topHabitName(topHabitName)
                .topHabitStreak(topHabitStreak)
                .performanceGrade(grade)
                .motivationalMessage(message)
                .build();
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    private int countHabitsCompletedInRange(Long userId,
                                            LocalDate start, LocalDate end) {
        List<Habit> habits = habitRepository.findByUserId(userId);
        int count = 0;
        for (Habit habit : habits) {
            List<HabitLog> logs = habitLogRepository
                    .findByHabitAndDate(habit, start);
            // Count all DONE logs in the date range
            for (LocalDate date = start;
                 !date.isAfter(end);
                 date = date.plusDays(1)) {
                final LocalDate d = date;
                boolean done = habitLogRepository
                        .findByHabitAndDate(habit, d)
                        .stream()
                        .anyMatch(l -> l.getStatus() == HabitLog.LogStatus.DONE);
                if (done) count++;
            }
        }
        return count;
    }

    private String getDominantMood(List<JournalEntry> entries) {
        if (entries.isEmpty()) return "No entries";

        return entries.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        JournalEntry::getMood,
                        java.util.stream.Collectors.counting()
                ))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(e -> e.getKey().name())
                .orElse("NEUTRAL");
    }

    private String calculateGrade(int completionPercent,
                                  int focusMinutes, int journalCount) {
        int score = 0;
        if (completionPercent >= 80) score += 3;
        else if (completionPercent >= 50) score += 2;
        else if (completionPercent >= 20) score += 1;

        if (focusMinutes >= 120) score += 3;
        else if (focusMinutes >= 60) score += 2;
        else if (focusMinutes >= 20) score += 1;

        if (journalCount >= 5) score += 3;
        else if (journalCount >= 3) score += 2;
        else if (journalCount >= 1) score += 1;

        if (score >= 8) return "S";
        if (score >= 6) return "A";
        if (score >= 4) return "B";
        return "C";
    }

    private String getMotivationalMessage(String grade, String username) {
        return switch (grade) {
            case "S" -> "Incredible week, " + username
                    + "! You're operating at peak performance. "
                    + "Keep this momentum going!";
            case "A" -> "Great week, " + username
                    + "! You're building real discipline. "
                    + "Push just a little harder next week!";
            case "B" -> "Solid effort, " + username
                    + "! You're making progress. "
                    + "Focus on consistency and the results will compound.";
            default  -> "Every expert was once a beginner, " + username
                    + ". This week is a fresh start — "
                    + "pick one habit and commit to it daily.";
        };
    }
}
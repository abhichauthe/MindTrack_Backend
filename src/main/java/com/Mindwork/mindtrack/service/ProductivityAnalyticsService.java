package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.AnalyticsDto;
import com.Mindwork.mindtrack.entity.DailyTask;
import com.Mindwork.mindtrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductivityAnalyticsService {

    private final DailyTaskRepository        dailyTaskRepository;
    private final FocusSessionRepository     focusSessionRepository;
    private final HabitLogRepository         habitLogRepository;

    public AnalyticsDto.ProductivityReport getWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // Tasks this week
        List<DailyTask> weekTasks = dailyTaskRepository
                .findByUserIdAndDueDateBetweenOrderByDueDateAscPriorityAsc(
                        userId, weekStart, today);

        long totalTasks     = weekTasks.size();
        long completedTasks = weekTasks.stream()
                .filter(t -> t.getStatus() == DailyTask.TaskStatus.DONE).count();
        long missedTasks    = weekTasks.stream()
                .filter(t -> t.getStatus() == DailyTask.TaskStatus.PENDING
                        && t.getDueDate().isBefore(today)).count();

        int completionRate = totalTasks > 0
                ? (int) ((completedTasks * 100) / totalTasks) : 0;

        // Planned vs actual time
        int plannedMinutes = weekTasks.stream()
                .filter(t -> t.getDurationMinutes() != null)
                .mapToInt(DailyTask::getDurationMinutes).sum();
        int actualMinutes  = weekTasks.stream()
                .filter(t -> t.getActualMinutesSpent() != null)
                .mapToInt(DailyTask::getActualMinutesSpent).sum();

        // Most productive hour (when most tasks were completed)
        Map<Integer, Long> completionsByHour = weekTasks.stream()
                .filter(t -> t.getStatus() == DailyTask.TaskStatus.DONE
                        && t.getDueTime() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getDueTime().getHour(),
                        Collectors.counting()
                ));

        int mostProductiveHour = completionsByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(9);

        // Completion by day
        List<AnalyticsDto.DayStats> dailyStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            List<DailyTask> dayTasks = weekTasks.stream()
                    .filter(t -> t.getDueDate().equals(day))
                    .collect(Collectors.toList());
            long done  = dayTasks.stream()
                    .filter(t -> t.getStatus() == DailyTask.TaskStatus.DONE).count();
            dailyStats.add(new AnalyticsDto.DayStats(
                    day, dayTasks.size(), (int) done
            ));
        }

        // Category breakdown
        Map<String, Long> byCategory = weekTasks.stream()
                .filter(t -> t.getStatus() == DailyTask.TaskStatus.DONE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.counting()
                ));

        // Snooze behavior
        int totalSnoozes = weekTasks.stream()
                .mapToInt(t -> t.getSnoozeCount() != null ? t.getSnoozeCount() : 0)
                .sum();

        String insight = generateInsight(
                completionRate, mostProductiveHour, totalSnoozes, missedTasks
        );

        return new AnalyticsDto.ProductivityReport(
                weekStart, today,
                (int) totalTasks, (int) completedTasks,
                (int) missedTasks, completionRate,
                plannedMinutes, actualMinutes,
                mostProductiveHour, totalSnoozes,
                dailyStats, byCategory, insight
        );
    }

    private String generateInsight(int rate, int peakHour,
                                   int snoozes, long missed) {
        String hourStr = peakHour < 12
                ? peakHour + " AM"
                : (peakHour == 12 ? "12 PM" : (peakHour - 12) + " PM");

        if (rate >= 80) {
            return "Excellent week! You completed " + rate + "% of tasks. "
                    + "Your peak hour is " + hourStr + " — protect that time.";
        } else if (snoozes > 5) {
            return "You snoozed " + snoozes + " times this week. "
                    + "Consider scheduling tasks closer to your peak hour (" + hourStr + ").";
        } else if (missed > 3) {
            return "You missed " + missed + " tasks. "
                    + "Try auto time-blocking to prevent over-scheduling.";
        } else {
            return "You completed " + rate + "% of tasks. "
                    + "Your most productive hour is " + hourStr + ". Schedule important tasks then.";
        }
    }
}
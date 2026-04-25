package com.Mindwork.mindtrack.scheduler;

import com.Mindwork.mindtrack.entity.DailyTask;
import com.Mindwork.mindtrack.entity.Notification;
import com.Mindwork.mindtrack.repository.DailyTaskRepository;
import com.Mindwork.mindtrack.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskNotificationScheduler {

    private final DailyTaskRepository dailyTaskRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    public void checkTaskNotifications() {

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        // ✅ Matches the 1-param overload in your repository
        List<DailyTask> scheduledTasks =
                dailyTaskRepository.findScheduledTasksForToday(today);

        for (DailyTask task : scheduledTasks) {

            if (task.getDueTime() == null) continue;

            Long userId        = task.getUser().getId();
            LocalTime taskTime = task.getDueTime();

            // ── Pre-reminder ──────────────────────────────────────────
            if (!Boolean.TRUE.equals(task.getPreReminderSent())
                    && isWithinWindow(now, taskTime.minusMinutes(10), taskTime.minusMinutes(9))) {

                notificationService.createNotification(
                        userId,
                        "⏰ Starting in 10 minutes",
                        "\"" + task.getTitle() + "\" starts at " + formatTime(taskTime),
                        Notification.NotificationType.HABIT_REMINDER
                );
                task.setPreReminderSent(true);
                dailyTaskRepository.save(task);
                log.info("Pre-reminder sent for task {}", task.getId());
            }

            // ── Start notification ────────────────────────────────────
            if (!Boolean.TRUE.equals(task.getStartNotifSent())
                    && isWithinWindow(now, taskTime, taskTime.plusMinutes(1))) {

                notificationService.createNotification(
                        userId,
                        "🎯 Task starting now!",
                        "Task \"" + task.getTitle() + "\" is starting now.",
                        Notification.NotificationType.FOCUS_COMPLETE
                );
                task.setStartNotifSent(true);
                dailyTaskRepository.save(task);
                log.info("Start notification sent for task {}", task.getId());
            }

            // ── Missed task alert ─────────────────────────────────────
            // ✅ Fixed: PENDING (not COMPLETED) matches your TaskStatus enum
            if (!Boolean.TRUE.equals(task.getMissedNotifSent())
                    && task.getStatus() == DailyTask.TaskStatus.PENDING
                    && isWithinWindow(now, taskTime.plusMinutes(15), taskTime.plusMinutes(16))) {

                notificationService.createNotification(
                        userId,
                        "❌ Missed task",
                        "\"" + task.getTitle() + "\" was scheduled at " + formatTime(taskTime),
                        Notification.NotificationType.SYSTEM
                );
                task.setMissedNotifSent(true);
                dailyTaskRepository.save(task);
                log.info("Missed notification sent for task {}", task.getId());
            }
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void detectActiveConflicts() {

        LocalDate today = LocalDate.now();
        LocalTime now   = LocalTime.now();

        // ✅ Matches findCurrentlyActiveTasks(LocalDate, LocalTime) in your repo
        List<DailyTask> activeTasks =
                dailyTaskRepository.findCurrentlyActiveTasks(today, now);

        activeTasks = activeTasks.stream()
                .filter(task -> {
                    if (task.getDueTime() == null || task.getDurationMinutes() == null)
                        return false;
                    LocalTime end = task.getDueTime().plusMinutes(task.getDurationMinutes());
                    return now.isBefore(end);
                })
                .toList();

        if (activeTasks.size() > 1) {
            List<DailyTask> finalActiveTasks = activeTasks;
            activeTasks.forEach(task -> {
                String otherTask = finalActiveTasks.stream()
                        .filter(t -> !t.getId().equals(task.getId()))
                        .map(DailyTask::getTitle)
                        .findFirst()
                        .orElse("another task");

                notificationService.createNotification(
                        task.getUser().getId(),
                        "⚠️ Schedule Conflict",
                        "\"" + task.getTitle() + "\" conflicts with \"" + otherTask + "\"",
                        Notification.NotificationType.SYSTEM
                );
            });
            log.warn("Conflict detected among {} tasks", activeTasks.size());
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void rescheduleMissedTasksDaily() {
        log.info("Running daily reschedule job...");
    }

    private boolean isWithinWindow(LocalTime now, LocalTime from, LocalTime to) {
        return !now.isBefore(from) && now.isBefore(to);
    }

    private String formatTime(LocalTime t) {
        int h  = t.getHour();
        int m  = t.getMinute();
        String p = h < 12 ? "AM" : "PM";
        int hr = h % 12 == 0 ? 12 : h % 12;
        return hr + ":" + String.format("%02d", m) + " " + p;
    }
}
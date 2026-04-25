package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.entity.*;
import com.Mindwork.mindtrack.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSyncService {

    private final DailyTaskRepository dailyTaskRepository;
    private final TimeBlockRepository timeBlockRepository; // ✅ FIXED
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ── Sync a single task to timetable ──────────────────────────────
    public void syncTaskToTimetable(DailyTask task) {

        if (task.getDueTime() == null || task.getDurationMinutes() == null) return;

        LocalTime startTime = task.getDueTime();
        LocalTime endTime   = startTime.plusMinutes(task.getDurationMinutes());

        // Check conflicts
        List<TimeBlock> conflicts = timeBlockRepository.findConflictingBlocks(
                task.getUser().getId(),
                task.getDueDate(),
                startTime,
                endTime
        );

        if (!conflicts.isEmpty()) {
            notificationService.createNotification(
                    task.getUser().getId(),
                    "⚠️ Schedule Conflict",
                    "\"" + task.getTitle() + "\" conflicts with \""
                            + conflicts.get(0).getTitle() + "\"",
                    Notification.NotificationType.SYSTEM
            );
        }

        // Remove old block if exists
        if (task.getTimetableBlock() != null) {
            timeBlockRepository.delete(task.getTimetableBlock());
        }

        // Create new block
        TimeBlock block = TimeBlock.builder()
                .user(task.getUser())
                .task(task)
                .title(task.getTitle())
                .description(task.getDescription())
                .category(mapCategory(task.getCategory()))
                .startTime(startTime)
                .endTime(endTime)
                .date(task.getDueDate())
                .status(TimeBlock.BlockStatus.PENDING)
                .autoScheduled(false)
                .build();

        TimeBlock savedBlock = timeBlockRepository.save(block);

        // Link back
        task.setTimetableBlock(savedBlock);
        task.setSyncedToTimetable(true);
        task.setScheduledAt(LocalDateTime.of(task.getDueDate(), startTime));
        dailyTaskRepository.save(task);

        log.info("Task {} synced successfully", task.getId());
    }

    // ── Auto scheduling ──────────────────────────────────────────────
    public int autoBlockDay(Long userId, LocalDate date) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DailyTask> tasks =
                dailyTaskRepository.findUnscheduledTasksForDate(userId, date);

        if (tasks.isEmpty()) return 0;

        List<TimeBlock> existing =
                timeBlockRepository.findByUserIdAndDateOrderByStartTimeAsc(userId, date);

        LocalTime cursor = LocalTime.of(9, 0);
        int scheduled = 0;

        for (DailyTask task : tasks) {

            int duration = task.getDurationMinutes() != null
                    ? task.getDurationMinutes() : 30;

            LocalTime slot = findNextFreeSlot(cursor, duration, existing);
            if (slot == null) break;

            task.setDueTime(slot);
            task.setDurationMinutes(duration);
            dailyTaskRepository.save(task);

            syncTaskToTimetable(task);

            cursor = slot.plusMinutes(duration + 15);
            scheduled++;

            existing = timeBlockRepository
                    .findByUserIdAndDateOrderByStartTimeAsc(userId, date);
        }

        if (scheduled > 0) {
            notificationService.createNotification(
                    userId,
                    "📅 Auto Schedule Done",
                    scheduled + " tasks scheduled for today",
                    Notification.NotificationType.SYSTEM
            );
        }

        return scheduled;
    }

    // ── Reschedule missed tasks ──────────────────────────────────────
    public int rescheduleMissedTasks(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<DailyTask> missed =
                dailyTaskRepository.findMissedTasks(userId, yesterday);

        int count = 0;

        for (DailyTask task : missed) {
            task.setDueDate(today);
            task.setDueTime(null);
            task.setSyncedToTimetable(false);
            task.setTimetableBlock(null);
            task.setPreReminderSent(false);
            task.setStartNotifSent(false);
            task.setMissedNotifSent(false);

            dailyTaskRepository.save(task);
            count++;
        }

        if (count > 0) {
            notificationService.createNotification(
                    userId,
                    "🔄 Tasks Rescheduled",
                    count + " tasks moved to today",
                    Notification.NotificationType.SYSTEM
            );
        }

        return count;
    }

    // ── User actions ─────────────────────────────────────────────────
    public void syncTaskById(Long userId, Long taskId) {

        DailyTask task = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        syncTaskToTimetable(task);
    }

    public void snoozeTask(Long userId, Long taskId, int minutes) {

        DailyTask task = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getDueTime() != null) {
            task.setDueTime(task.getDueTime().plusMinutes(minutes));
            task.setStartNotifSent(false);
            task.setMissedNotifSent(false);

            dailyTaskRepository.save(task);
            syncTaskToTimetable(task);
        }
    }

    public void skipTask(Long userId, Long taskId) {

        DailyTask task = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(DailyTask.TaskStatus.SKIPPED);
        dailyTaskRepository.save(task);

        if (task.getTimetableBlock() != null) {
            timeBlockRepository.delete(task.getTimetableBlock());
            task.setTimetableBlock(null);
            task.setSyncedToTimetable(false);
            dailyTaskRepository.save(task);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private LocalTime findNextFreeSlot(LocalTime from, int duration,
                                       List<TimeBlock> existing) {

        LocalTime endOfDay = LocalTime.of(22, 0);
        LocalTime candidate = from;

        while (!candidate.plusMinutes(duration).isAfter(endOfDay)) {

            final LocalTime start = candidate;
            final LocalTime end   = candidate.plusMinutes(duration);

            boolean conflict = existing.stream().anyMatch(block ->
                    start.isBefore(block.getEndTime()) &&
                            end.isAfter(block.getStartTime())
            );

            if (!conflict) return start;

            candidate = existing.stream()
                    .filter(b -> start.isBefore(b.getEndTime()) &&
                            end.isAfter(b.getStartTime()))
                    .map(TimeBlock::getEndTime)
                    .max(LocalTime::compareTo)
                    .orElse(candidate.plusMinutes(30));
        }

        return null;
    }

    private TimeBlock.BlockCategory mapCategory(DailyTask.TaskCategory cat) {
        return switch (cat) {
            case WORK       -> TimeBlock.BlockCategory.WORK;
            case DISCIPLINE -> TimeBlock.BlockCategory.DISCIPLINE;
            case HEALTH     -> TimeBlock.BlockCategory.PERSONAL;
            default         -> TimeBlock.BlockCategory.PERSONAL;
        };
    }
}

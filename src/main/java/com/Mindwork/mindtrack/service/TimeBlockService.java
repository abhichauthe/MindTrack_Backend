package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.TimeBlockDto;
import com.Mindwork.mindtrack.entity.DailyTask;
import com.Mindwork.mindtrack.entity.Habit;
import com.Mindwork.mindtrack.entity.TimeBlock;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.DailyTaskRepository;
import com.Mindwork.mindtrack.repository.HabitRepository;
import com.Mindwork.mindtrack.repository.TimeBlockRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeBlockService {

    private final TimeBlockRepository timeBlockRepository;
    private final UserRepository      userRepository;
    private final HabitRepository     habitRepository;
    private final DailyTaskRepository dailyTaskRepository; // ✅ ADDED

    // ── Create a new time block ───────────────────────────────────────────
    public TimeBlockDto.TimeBlockResponse createBlock(Long userId,
                                                      TimeBlockDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        TimeBlock.TimeBlockBuilder builder = TimeBlock.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .category(request.getCategory() != null
                        ? request.getCategory()
                        : TimeBlock.BlockCategory.PERSONAL)
                .status(TimeBlock.BlockStatus.PENDING);

        if (request.getHabitId() != null) {
            Habit habit = habitRepository.findById(request.getHabitId()).orElse(null);
            if (habit != null) {
                builder.habit(habit);
                builder.category(TimeBlock.BlockCategory.HABIT);
            }
        }

        TimeBlock saved = timeBlockRepository.save(builder.build());
        return new TimeBlockDto.TimeBlockResponse(saved);
    }

    // ── Get daily schedule with summary ──────────────────────────────────
    public TimeBlockDto.DailySummaryResponse getDailySchedule(Long userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<TimeBlockDto.TimeBlockResponse> blocks = timeBlockRepository
                .findByUserIdAndDateOrderByStartTimeAsc(userId, targetDate)
                .stream()
                .map(TimeBlockDto.TimeBlockResponse::new)
                .collect(Collectors.toList());

        long total     = timeBlockRepository.countByUserIdAndDate(userId, targetDate);
        long completed = timeBlockRepository.countByUserIdAndDateAndStatus(
                userId, targetDate, TimeBlock.BlockStatus.DONE);

        return new TimeBlockDto.DailySummaryResponse(targetDate, total, completed, blocks);
    }

    // ── Get blocks for a date range ───────────────────────────────────────
    public List<TimeBlockDto.TimeBlockResponse> getBlocksInRange(Long userId,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        return timeBlockRepository
                .findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(userId, startDate, endDate)
                .stream()
                .map(TimeBlockDto.TimeBlockResponse::new)
                .collect(Collectors.toList());
    }

    // ── Update a time block ───────────────────────────────────────────────
    public TimeBlockDto.TimeBlockResponse updateBlock(Long userId, Long blockId,
                                                      TimeBlockDto.UpdateRequest request) {
        TimeBlock block = getBlockOwnedByUser(userId, blockId);

        if (request.getTitle()       != null) block.setTitle(request.getTitle());
        if (request.getDescription() != null) block.setDescription(request.getDescription());
        if (request.getDate()        != null) block.setDate(request.getDate());
        if (request.getCategory()    != null) block.setCategory(request.getCategory());
        if (request.getStatus()      != null) block.setStatus(request.getStatus());
        if (request.getStartTime()   != null) block.setStartTime(request.getStartTime());
        if (request.getEndTime()     != null) block.setEndTime(request.getEndTime());

        if (!block.getEndTime().isAfter(block.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        return new TimeBlockDto.TimeBlockResponse(timeBlockRepository.save(block));
    }

    // ── Update status only ────────────────────────────────────────────────
    public TimeBlockDto.TimeBlockResponse updateStatus(Long userId, Long blockId,
                                                       TimeBlockDto.StatusUpdateRequest request) {
        TimeBlock block = getBlockOwnedByUser(userId, blockId);
        block.setStatus(request.getStatus());
        return new TimeBlockDto.TimeBlockResponse(timeBlockRepository.save(block));
    }

    // ── Delete a time block ───────────────────────────────────────────────
    public void deleteBlock(Long userId, Long blockId) {
        TimeBlock block = getBlockOwnedByUser(userId, blockId);
        timeBlockRepository.delete(block);
    }

    // ── Pull habits as suggested blocks (not yet saved) ──────────────────
    public List<TimeBlockDto.TimeBlockResponse> getHabitSuggestions(Long userId) {
        return habitRepository.findByUserId(userId).stream()
                .map(habit -> {
                    TimeBlock suggestion = TimeBlock.builder()
                            .user(null)
                            .title(habit.getName())
                            .description(habit.getDescription())
                            .date(LocalDate.now())
                            .startTime(LocalTime.of(8, 0))
                            .endTime(LocalTime.of(8, 30))
                            .category(TimeBlock.BlockCategory.HABIT)
                            .status(TimeBlock.BlockStatus.PENDING)
                            .habit(habit)
                            .build();
                    suggestion.setId(habit.getId());
                    return new TimeBlockDto.TimeBlockResponse(suggestion);
                })
                .collect(Collectors.toList());
    }

    // ── ✅ NEW: Pull today's pending plan tasks as timetable suggestions ──
    //
    // When the user opens the timetable builder, this method returns all
    // DailyTasks from their Monthly Plan that are due today and not yet
    // synced to the timetable — so they can drag/add them as time blocks.
    public List<TimeBlockDto.TimeBlockResponse> getTodayPlanTaskSuggestions(Long userId) {
        LocalDate today = LocalDate.now();

        return dailyTaskRepository
                .findByUserIdAndDueDateOrderByPriorityAscDueTimeAsc(userId, today)
                .stream()
                // Only show tasks not yet added to the timetable
                .filter(task -> !task.isSyncedToTimetable()
                        && task.getStatus() == DailyTask.TaskStatus.PENDING)
                .map(task -> {
                    // Map the DailyTask to an unsaved TimeBlock suggestion
                    LocalTime start = task.getDueTime() != null
                            ? task.getDueTime()
                            : LocalTime.of(9, 0); // fallback if no time set

                    int duration = task.getDurationMinutes() != null
                            ? task.getDurationMinutes()
                            : 30;

                    TimeBlock suggestion = TimeBlock.builder()
                            .user(task.getUser())
                            .title(task.getTitle())
                            .description(task.getDescription())
                            .date(today)
                            .startTime(start)
                            .endTime(start.plusMinutes(duration))
                            .category(mapTaskCategory(task.getCategory()))
                            .status(TimeBlock.BlockStatus.PENDING)
                            .task(task) // ✅ links back to the DailyTask
                            .build();

                    // Use task id so frontend knows which DailyTask this maps to
                    suggestion.setId(task.getId());
                    return new TimeBlockDto.TimeBlockResponse(suggestion);
                })
                .collect(Collectors.toList());
    }

    // ── ✅ NEW: Combined suggestions (habits + today's plan tasks) ────────
    //
    // Single endpoint the frontend calls when opening the timetable.
    // Returns both habit suggestions and today's pending plan tasks together.
    public List<TimeBlockDto.TimeBlockResponse> getAllTimetableSuggestions(Long userId) {
        List<TimeBlockDto.TimeBlockResponse> all = new ArrayList<>();
        all.addAll(getTodayPlanTaskSuggestions(userId)); // plan tasks first (higher priority)
        all.addAll(getHabitSuggestions(userId));         // then habits
        return all;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private TimeBlock getBlockOwnedByUser(Long userId, Long blockId) {
        TimeBlock block = timeBlockRepository.findById(blockId)
                .orElseThrow(() -> new RuntimeException("Time block not found"));
        if (!block.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return block;
    }

    // Maps DailyTask category → TimeBlock category
    private TimeBlock.BlockCategory mapTaskCategory(DailyTask.TaskCategory cat) {
        if (cat == null) return TimeBlock.BlockCategory.PERSONAL;
        return switch (cat) {
            case WORK       -> TimeBlock.BlockCategory.WORK;
            case STUDY      -> TimeBlock.BlockCategory.WORK;
            case DISCIPLINE -> TimeBlock.BlockCategory.WORK;
            case HEALTH     -> TimeBlock.BlockCategory.PERSONAL;
            default         -> TimeBlock.BlockCategory.PERSONAL;
        };
    }
}
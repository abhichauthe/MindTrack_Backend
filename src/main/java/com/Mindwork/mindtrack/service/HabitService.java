package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.HabitDto;
import com.Mindwork.mindtrack.entity.Habit;
import com.Mindwork.mindtrack.entity.HabitLog;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.HabitLogRepository;
import com.Mindwork.mindtrack.repository.HabitRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository      habitRepository;
    private final HabitLogRepository   habitLogRepository;
    private final UserRepository       userRepository;
    private final GamificationService  gamificationService; // ← added

    public HabitDto.HabitResponse createHabit(Long userId, HabitDto.CreateHabitRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Habit habit = Habit.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Habit saved = habitRepository.save(habit);
        return toHabitResponse(saved, false);
    }

    public List<HabitDto.HabitResponse> getHabitsByUser(Long userId) {
        List<Habit> habits = habitRepository.findByUserId(userId);
        LocalDate today = LocalDate.now();

        return habits.stream()
                .map(habit -> {
                    boolean completedToday = habitLogRepository
                            .findByHabitAndDate(habit, today)
                            .stream()
                            .anyMatch(log -> log.getStatus() == HabitLog.LogStatus.DONE);
                    return toHabitResponse(habit, completedToday);
                })
                .collect(Collectors.toList());
    }

    public HabitDto.HabitLogResponse logHabit(Long habitId, HabitDto.LogHabitRequest request) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        LocalDate logDate = request.getDate() != null
                ? request.getDate() : LocalDate.now();

        HabitLog.LogStatus status = request.getStatus() != null
                ? request.getStatus() : HabitLog.LogStatus.DONE;

        // Update if already logged today, else create new
        List<HabitLog> existingLogs = habitLogRepository
                .findByHabitAndDate(habit, logDate);
        HabitLog log;

        if (!existingLogs.isEmpty()) {
            log = existingLogs.get(0);
            log.setStatus(status);
        } else {
            log = HabitLog.builder()
                    .habit(habit)
                    .date(logDate)
                    .status(status)
                    .build();
        }

        HabitLog saved = habitLogRepository.save(log);

        // ── Gamification — XP + streak + badges ──────────────────────
        if (saved.getStatus() == HabitLog.LogStatus.DONE) {
            gamificationService.onHabitCompleted(
                    habit.getUser().getId(), habit
            );
        }

        return toHabitLogResponse(saved);
    }

    // ── Private Mappers ───────────────────────────────────────────────────

    private HabitDto.HabitResponse toHabitResponse(Habit habit,
                                                   boolean completedToday) {
        return new HabitDto.HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.getCreatedAt(),
                completedToday
        );
    }

    private HabitDto.HabitLogResponse toHabitLogResponse(HabitLog log) {
        return new HabitDto.HabitLogResponse(
                log.getId(),
                log.getHabit().getId(),
                log.getDate(),
                log.getStatus(),
                log.getLoggedAt()
        );
    }
}
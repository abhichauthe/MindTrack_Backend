package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.DisciplineDto;
import com.Mindwork.mindtrack.entity.Discipline;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.DisciplineRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class
DisciplineService {

    private final DisciplineRepository disciplineRepository;
    private final UserRepository       userRepository;

    // ── Create from wizard completion ─────────────────────────────────
    public DisciplineDto.DisciplineResponse create(Long userId,
                                                   DisciplineDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Discipline.ScheduleType scheduleType;
        try {
            scheduleType = Discipline.ScheduleType.valueOf(
                    request.getScheduleType().toUpperCase());
        } catch (Exception e) {
            scheduleType = Discipline.ScheduleType.DAILY;
        }

        String scheduleDays = null;
        if (scheduleType == Discipline.ScheduleType.SPECIFIC
                && request.getDays() != null
                && !request.getDays().isEmpty()) {
            scheduleDays = String.join(",", request.getDays());
        }

        Discipline discipline = Discipline.builder()
                .user(user)
                .areaName(request.getAreaName())
                .areaId(request.getAreaId())
                .areaEmoji(request.getAreaEmoji() != null ? request.getAreaEmoji() : "🎯")
                .behavior(request.getBehavior())
                .dailyAction(request.getDailyAction())
                .scheduleType(scheduleType)
                .scheduleDays(scheduleDays)
                .status(Discipline.DisciplineStatus.ACTIVE)
                // In DisciplineService.create() — add these two lines inside the builder
                .framework(request.getFramework())
                .weeklyPlan(request.getWeeklyPlan())
                .build();

        return new DisciplineDto.DisciplineResponse(
                disciplineRepository.save(discipline));
    }

    // ── Get all for user ──────────────────────────────────────────────
    public List<DisciplineDto.DisciplineResponse> getAll(Long userId) {
        return disciplineRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(DisciplineDto.DisciplineResponse::new)
                .collect(Collectors.toList());
    }

    // ── Get active only ───────────────────────────────────────────────
    public List<DisciplineDto.DisciplineResponse> getActive(Long userId) {
        return disciplineRepository
                .findByUserIdAndStatus(userId, Discipline.DisciplineStatus.ACTIVE)
                .stream()
                .map(DisciplineDto.DisciplineResponse::new)
                .collect(Collectors.toList());
    }

    // ── Update ────────────────────────────────────────────────────────
    public DisciplineDto.DisciplineResponse update(Long userId, Long id,
                                                   DisciplineDto.UpdateRequest request) {
        Discipline d = getOwnedByUser(userId, id);

        if (request.getBehavior()   != null) d.setBehavior(request.getBehavior());
        if (request.getDailyAction()!= null) d.setDailyAction(request.getDailyAction());

        if (request.getStatus() != null) {
            try { d.setStatus(Discipline.DisciplineStatus.valueOf(request.getStatus())); }
            catch (Exception ignored) {}
        }

        if (request.getScheduleType() != null) {
            try {
                Discipline.ScheduleType st = Discipline.ScheduleType.valueOf(
                        request.getScheduleType().toUpperCase());
                d.setScheduleType(st);
                if (st == Discipline.ScheduleType.SPECIFIC && request.getDays() != null) {
                    d.setScheduleDays(String.join(",", request.getDays()));
                }
            } catch (Exception ignored) {}
        }

        return new DisciplineDto.DisciplineResponse(disciplineRepository.save(d));
    }

    // ── Delete ────────────────────────────────────────────────────────
    public void delete(Long userId, Long id) {
        disciplineRepository.delete(getOwnedByUser(userId, id));
    }

    // ── Private helper ────────────────────────────────────────────────
    private Discipline getOwnedByUser(Long userId, Long id) {
        Discipline d = disciplineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discipline not found"));
        if (!d.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return d;
    }
}
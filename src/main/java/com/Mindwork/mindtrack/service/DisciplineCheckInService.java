package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.CheckInDto;
import com.Mindwork.mindtrack.entity.Discipline;
import com.Mindwork.mindtrack.entity.DisciplineCheckIn;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.DisciplineCheckInRepository;
import com.Mindwork.mindtrack.repository.DisciplineRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisciplineCheckInService {

    private final DisciplineCheckInRepository checkInRepository;
    private final DisciplineRepository        disciplineRepository;
    private final UserRepository              userRepository;
    private final ObjectMapper                objectMapper;
    private final NotificationService         notificationService;

    // ── Submit a check-in ─────────────────────────────────────────────
    public CheckInDto.CheckInResponse submitCheckIn(Long userId, Long disciplineId,
                                                    CheckInDto.CheckInRequest request) {
        Discipline discipline = disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new RuntimeException("Discipline not found"));

        if (!discipline.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate checkInDate = request.getDate() != null
                ? request.getDate() : LocalDate.now();

        boolean alreadyDone = checkInRepository
                .existsByDisciplineIdAndDate(disciplineId, checkInDate);

        // Convert answers list to JSON string for storage
        String answersJson = serializeAnswers(request.getAnswers());

        DisciplineCheckIn checkIn;

        if (alreadyDone) {
            // Update existing check-in for today
            checkIn = checkInRepository
                    .findByDisciplineIdAndDate(disciplineId, checkInDate)
                    .orElseThrow();
            checkIn.setAnswers(answersJson);
        } else {
            // Create new check-in
            checkIn = DisciplineCheckIn.builder()
                    .discipline(discipline)
                    .user(user)
                    .answers(answersJson)
                    .date(checkInDate)
                    .build();
        }

        DisciplineCheckIn saved = checkInRepository.save(checkIn);

        // Fire notification
        notificationService.createNotification(
                userId,
                "Check-in complete! ✓",
                "Daily check-in for \"" + discipline.getAreaName()
                        + "\" recorded. Keep the streak going!",
                com.Mindwork.mindtrack.entity.Notification.NotificationType.HABIT_REMINDER
        );

        return toResponse(saved, alreadyDone);
    }

    // ── Get all check-ins for a discipline ────────────────────────────
    public CheckInDto.CheckInSummary getCheckIns(Long userId, Long disciplineId) {
        Discipline discipline = disciplineRepository.findById(disciplineId)
                .orElseThrow(() -> new RuntimeException("Discipline not found"));

        if (!discipline.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<DisciplineCheckIn> all = checkInRepository
                .findByDisciplineIdOrderByDateDesc(disciplineId);

        boolean checkedInToday = checkInRepository
                .existsByDisciplineIdAndDate(disciplineId, LocalDate.now());

        long total = checkInRepository.countByDisciplineId(disciplineId);

        List<CheckInDto.CheckInResponse> recent = all.stream()
                .limit(10)
                .map(c -> toResponse(c, false))
                .collect(Collectors.toList());

        return new CheckInDto.CheckInSummary(total, checkedInToday, recent);
    }

    // ── Private helpers ───────────────────────────────────────────────
    private String serializeAnswers(List<Object> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            log.error("Failed to serialize answers", e);
            return "[]";
        }
    }

    private List<Object> deserializeAnswers(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize answers", e);
            return List.of();
        }
    }

    private CheckInDto.CheckInResponse toResponse(DisciplineCheckIn c,
                                                  boolean alreadyDone) {
        return new CheckInDto.CheckInResponse(
                c.getId(),
                c.getDiscipline().getId(),
                deserializeAnswers(c.getAnswers()),
                c.getDate(),
                c.getCreatedAt(),
                alreadyDone
        );
    }
}
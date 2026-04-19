package com.Mindwork.mindtrack.service;


import com.Mindwork.mindtrack.dto.FocusDto;
import com.Mindwork.mindtrack.entity.FocusSession;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.FocusSessionRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FocusService {

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;

    public FocusDto.FocusSessionResponse saveSession(Long userId, FocusDto.SaveSessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FocusSession session = FocusSession.builder()
                .user(user)
                .durationMinutes(request.getDurationMinutes())
                .type(request.getType() != null ? request.getType() : FocusSession.SessionType.FOCUS)
                .status(request.getStatus() != null ? request.getStatus() : FocusSession.SessionStatus.COMPLETED)
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt() != null ? request.getCompletedAt() : LocalDateTime.now())
                .build();

        return new FocusDto.FocusSessionResponse(focusSessionRepository.save(session));
    }

    public List<FocusDto.FocusSessionResponse> getSessions(Long userId) {
        return focusSessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(FocusDto.FocusSessionResponse::new)
                .collect(Collectors.toList());
    }

    public FocusDto.FocusStatsResponse getStats(Long userId) {
        LocalDateTime startOfDay  = LocalDateTime.now().with(LocalTime.MIDNIGHT);
        LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);

        long totalSessions      = focusSessionRepository.countByUserIdAndStatus(userId, FocusSession.SessionStatus.COMPLETED);
        Integer minutesToday    = focusSessionRepository.getTotalFocusMinutesSince(userId, startOfDay);
        Integer minutesThisWeek = focusSessionRepository.getTotalFocusMinutesSince(userId, startOfWeek);

        return new FocusDto.FocusStatsResponse(totalSessions, minutesToday, minutesThisWeek);
    }
}
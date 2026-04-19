package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.FocusSession;
import lombok.Data;
import java.time.LocalDateTime;

public class FocusDto {

    @Data
    public static class SaveSessionRequest {
        private Integer durationMinutes;
        private FocusSession.SessionType type;
        private FocusSession.SessionStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    @Data
    public static class FocusSessionResponse {
        private Long id;
        private Integer durationMinutes;
        private FocusSession.SessionType type;
        private FocusSession.SessionStatus status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;

        public FocusSessionResponse(FocusSession s) {
            this.id              = s.getId();
            this.durationMinutes = s.getDurationMinutes();
            this.type            = s.getType();
            this.status          = s.getStatus();
            this.startedAt       = s.getStartedAt();
            this.completedAt     = s.getCompletedAt();
            this.createdAt       = s.getCreatedAt();
        }
    }

    @Data
    public static class FocusStatsResponse {
        private long totalSessions;
        private Integer totalMinutesToday;
        private Integer totalMinutesWeek;

        public FocusStatsResponse(long totalSessions, Integer totalMinutesToday, Integer totalMinutesWeek) {
            this.totalSessions     = totalSessions;
            this.totalMinutesToday = totalMinutesToday;
            this.totalMinutesWeek  = totalMinutesWeek;
        }
    }
}
package com.Mindwork.mindtrack.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CheckInDto {

    @Data
    public static class CheckInRequest {
        private List<Object> answers; // mixed types: Boolean, Integer, String
        private LocalDate date;
    }

    @Data
    public static class CheckInResponse {
        private Long id;
        private Long disciplineId;
        private List<Object> answers;
        private LocalDate date;
        private LocalDateTime createdAt;
        private boolean alreadyCheckedInToday;

        public CheckInResponse(Long id, Long disciplineId,
                               List<Object> answers, LocalDate date,
                               LocalDateTime createdAt,
                               boolean alreadyCheckedInToday) {
            this.id                   = id;
            this.disciplineId         = disciplineId;
            this.answers              = answers;
            this.date                 = date;
            this.createdAt            = createdAt;
            this.alreadyCheckedInToday = alreadyCheckedInToday;
        }
    }

    @Data
    public static class CheckInSummary {
        private long totalCheckIns;
        private boolean checkedInToday;
        private List<CheckInResponse> recent;

        public CheckInSummary(long totalCheckIns, boolean checkedInToday,
                              List<CheckInResponse> recent) {
            this.totalCheckIns  = totalCheckIns;
            this.checkedInToday = checkedInToday;
            this.recent         = recent;
        }
    }
}
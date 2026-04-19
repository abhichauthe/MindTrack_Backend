package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.JournalEntry;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class JournalDto {

    @Data
    public static class CreateEntryRequest {
        private String title;
        private String content;
        private JournalEntry.Mood mood;
        private LocalDate date;
    }

    @Data
    public static class UpdateEntryRequest {
        private String title;
        private String content;
        private JournalEntry.Mood mood;
    }

    @Data
    public static class JournalEntryResponse {
        private Long id;
        private String title;
        private String content;
        private JournalEntry.Mood mood;
        private LocalDate date;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public JournalEntryResponse(JournalEntry e) {
            this.id        = e.getId();
            this.title     = e.getTitle();
            this.content   = e.getContent();
            this.mood      = e.getMood();
            this.date      = e.getDate();
            this.createdAt = e.getCreatedAt();
            this.updatedAt = e.getUpdatedAt();
        }
    }
}
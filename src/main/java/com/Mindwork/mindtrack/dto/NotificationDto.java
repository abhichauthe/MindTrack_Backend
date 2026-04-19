package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.Notification;
import lombok.Data;
import java.time.LocalDateTime;

public class NotificationDto {

    @Data
    public static class NotificationResponse {
        private Long id;
        private String title;
        private String message;
        private Notification.NotificationType type;
        private boolean read;
        private LocalDateTime createdAt;

        public NotificationResponse(Notification n) {
            this.id        = n.getId();
            this.title     = n.getTitle();
            this.message   = n.getMessage();
            this.type      = n.getType();
            this.read      = n.isRead();
            this.createdAt = n.getCreatedAt();
        }
    }

    @Data
    public static class UnreadCountResponse {
        private long count;

        public UnreadCountResponse(long count) {
            this.count = count;
        }
    }
}
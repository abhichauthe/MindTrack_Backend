package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.NotificationDto;
import com.Mindwork.mindtrack.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto.NotificationResponse>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getAllNotifications(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto.NotificationResponse>> getUnread(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<NotificationDto.UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            notificationService.markAsRead(userId, id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    record ErrorResponse(String error) {}
}
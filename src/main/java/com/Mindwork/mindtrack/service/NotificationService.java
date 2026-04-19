package com.Mindwork.mindtrack.service;


import com.Mindwork.mindtrack.dto.NotificationDto;
import com.Mindwork.mindtrack.entity.Notification;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.NotificationRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Called internally by other services (e.g. after focus session completes)
    public void createNotification(Long userId, String title, String message, Notification.NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationDto.NotificationResponse> getAllNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto.NotificationResponse::new)
                .collect(Collectors.toList());
    }

    public List<NotificationDto.NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto.NotificationResponse::new)
                .collect(Collectors.toList());
    }

    public NotificationDto.UnreadCountResponse getUnreadCount(Long userId) {
        return new NotificationDto.UnreadCountResponse(
                notificationRepository.countByUserIdAndReadFalse(userId)
        );
    }

    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
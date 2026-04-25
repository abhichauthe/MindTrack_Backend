package com.Mindwork.mindtrack.scheduler;

import com.Mindwork.mindtrack.dto.WeeklyReviewDto;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.UserRepository;
import com.Mindwork.mindtrack.service.EmailService;
import com.Mindwork.mindtrack.service.WeeklyReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReviewScheduler {

    private final UserRepository      userRepository;
    private final WeeklyReviewService weeklyReviewService;
    private final EmailService        emailService;

    // Runs every Sunday at 8:00 AM
    @Scheduled(cron = "0 0 8 * * SUN")
    public void sendWeeklyReviews() {
        log.info("Starting weekly review email job...");

        List<User> allUsers = userRepository.findAll();
        int success = 0;
        int failed  = 0;

        for (User user : allUsers) {
            try {
                WeeklyReviewDto.WeeklyReport report =
                        weeklyReviewService.buildReportForUser(user);
                emailService.sendWeeklyReview(report);
                success++;
            } catch (Exception e) {
                log.error("Failed to send review for user {}: {}",
                        user.getEmail(), e.getMessage());
                failed++;
            }
        }

        log.info("Weekly review job complete. Success: {}, Failed: {}",
                success, failed);
    }

    // ── Manual trigger endpoint for testing ──────────────────────────────
    // Call this to test without waiting for Sunday
    public void triggerManually(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            WeeklyReviewDto.WeeklyReport report =
                    weeklyReviewService.buildReportForUser(user);
            emailService.sendWeeklyReview(report);
            log.info("Manual weekly review sent to: {}", user.getEmail());
        });
    }
}
package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.WeeklyReviewDto;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.UserRepository;
import com.Mindwork.mindtrack.scheduler.WeeklyReviewScheduler;
import com.Mindwork.mindtrack.service.WeeklyReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly-review")
@RequiredArgsConstructor
public class WeeklyReviewController {

    private final WeeklyReviewService   weeklyReviewService;
    private final WeeklyReviewScheduler weeklyReviewScheduler;
    private final UserRepository        userRepository;

    // Preview report data (no email sent)
    @GetMapping("/preview")
    public ResponseEntity<?> preview(
            @RequestHeader("X-User-Id") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        WeeklyReviewDto.WeeklyReport report =
                weeklyReviewService.buildReportForUser(user);
        return ResponseEntity.ok(report);
    }

    // Manually trigger email (for testing)
    @PostMapping("/send-now")
    public ResponseEntity<?> sendNow(
            @RequestHeader("X-User-Id") Long userId) {
        weeklyReviewScheduler.triggerManually(userId);
        return ResponseEntity.ok("Weekly review email sent!");
    }
}
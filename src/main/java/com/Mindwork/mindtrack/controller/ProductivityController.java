package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.AnalyticsDto;
import com.Mindwork.mindtrack.dto.PlanDto;
import com.Mindwork.mindtrack.service.ProductivityAnalyticsService;
import com.Mindwork.mindtrack.service.TaskSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/productivity")
@RequiredArgsConstructor
public class ProductivityController {

    private final TaskSyncService               taskSyncService;
    private final ProductivityAnalyticsService  analyticsService;

    // ── Sync a specific task to timetable ─────────────────────────────
    @PostMapping("/sync-task/{taskId}")
    public ResponseEntity<?> syncTask(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long taskId) {
        try {
            taskSyncService.syncTaskById(userId, taskId);
            return ResponseEntity.ok(Map.of("message", "Task synced to timetable"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Auto time-block an entire day ─────────────────────────────────
    @PostMapping("/auto-block")
    public ResponseEntity<?> autoBlock(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        int scheduled = taskSyncService.autoBlockDay(userId, target);
        return ResponseEntity.ok(Map.of(
                "scheduled", scheduled,
                "message", scheduled + " tasks auto-scheduled into timetable"
        ));
    }

    // ── Reschedule missed tasks ────────────────────────────────────────
    @PostMapping("/reschedule-missed")
    public ResponseEntity<?> rescheduleMissed(
            @RequestHeader("X-User-Id") Long userId) {
        int count = taskSyncService.rescheduleMissedTasks(userId);
        return ResponseEntity.ok(Map.of(
                "rescheduled", count,
                "message", count + " missed tasks moved to today"
        ));
    }

    // ── Snooze a task (push back 10 minutes) ──────────────────────────
    @PostMapping("/snooze/{taskId}")
    public ResponseEntity<?> snoozeTask(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "10") int minutes) {
        try {
            taskSyncService.snoozeTask(userId, taskId, minutes);
            return ResponseEntity.ok(Map.of(
                    "message", "Task snoozed by " + minutes + " minutes"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Skip a task ────────────────────────────────────────────────────
    @PostMapping("/skip/{taskId}")
    public ResponseEntity<?> skipTask(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long taskId) {
        try {
            taskSyncService.skipTask(userId, taskId);
            return ResponseEntity.ok(Map.of("message", "Task skipped"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Weekly analytics report ────────────────────────────────────────
    @GetMapping("/analytics/weekly")
    public ResponseEntity<AnalyticsDto.ProductivityReport> getWeeklyAnalytics(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(analyticsService.getWeeklyReport(userId));
    }
}
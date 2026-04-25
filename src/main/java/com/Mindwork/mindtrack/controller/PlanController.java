package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.PlanDto;
import com.Mindwork.mindtrack.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    // ── Monthly Plans ─────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<PlanDto.MonthlyPlanResponse>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(planService.getAllPlans(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlan(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(planService.getPlan(userId, id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPlan(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PlanDto.CreatePlanRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(planService.createPlan(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            planService.deletePlan(userId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ── Weekly Breakdowns ─────────────────────────────────────────────

    @GetMapping("/{planId}/weeks")
    public ResponseEntity<?> getWeeks(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long planId) {
        try {
            return ResponseEntity.ok(planService.getWeeks(userId, planId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ── Daily Tasks ───────────────────────────────────────────────────

    @GetMapping("/tasks/today")
    public ResponseEntity<PlanDto.TodaySummary> getToday(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(planService.getTodayTasks(userId));
    }

    @GetMapping("/tasks/range")
    public ResponseEntity<List<PlanDto.DailyTaskResponse>> getByRange(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(planService.getTasksByDateRange(userId, start, end));
    }

    @GetMapping("/weeks/{weekId}/tasks")
    public ResponseEntity<List<PlanDto.DailyTaskResponse>> getTasksByWeek(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long weekId) {
        return ResponseEntity.ok(planService.getTasksByWeek(userId, weekId));
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PlanDto.CreateTaskRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(planService.createTask(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody PlanDto.UpdateTaskRequest request) {
        try {
            return ResponseEntity.ok(planService.updateTask(userId, id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            planService.deleteTask(userId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String error) {}
}
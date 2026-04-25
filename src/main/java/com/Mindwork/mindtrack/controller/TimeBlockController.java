package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.TimeBlockDto;
import com.Mindwork.mindtrack.service.TimeBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimeBlockController {

    private final TimeBlockService timeBlockService;

    // GET /api/timetable/day?date=2024-04-21
    // Returns full daily schedule with summary stats
    @GetMapping("/day")
    public ResponseEntity<TimeBlockDto.DailySummaryResponse> getDailySchedule(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(timeBlockService.getDailySchedule(userId, date));
    }

    // GET /api/timetable/range?startDate=2024-04-21&endDate=2024-04-27
    // Returns blocks in a date range (weekly view - future)
    @GetMapping("/range")
    public ResponseEntity<List<TimeBlockDto.TimeBlockResponse>> getBlocksInRange(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                timeBlockService.getBlocksInRange(userId, startDate, endDate));
    }

    // GET /api/timetable/habit-suggestions
    // Returns user's habits as unscheduled block suggestions for the task panel
    @GetMapping("/habit-suggestions")
    public ResponseEntity<List<TimeBlockDto.TimeBlockResponse>> getHabitSuggestions(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(timeBlockService.getHabitSuggestions(userId));
    }

    // POST /api/timetable
    // Create a new time block
    @PostMapping
    public ResponseEntity<TimeBlockDto.TimeBlockResponse> createBlock(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody TimeBlockDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timeBlockService.createBlock(userId, request));
    }

    // PUT /api/timetable/{id}
    // Update an existing time block (title, time, category, etc.)
    @PutMapping("/{id}")
    public ResponseEntity<TimeBlockDto.TimeBlockResponse> updateBlock(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody TimeBlockDto.UpdateRequest request) {
        return ResponseEntity.ok(timeBlockService.updateBlock(userId, id, request));
    }

    // PATCH /api/timetable/{id}/status
    // Mark a block as done, skipped, in progress etc.
    @PatchMapping("/{id}/status")
    public ResponseEntity<TimeBlockDto.TimeBlockResponse> updateStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody TimeBlockDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(timeBlockService.updateStatus(userId, id, request));
    }

    // DELETE /api/timetable/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlock(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        timeBlockService.deleteBlock(userId, id);
        return ResponseEntity.noContent().build();
    }
}
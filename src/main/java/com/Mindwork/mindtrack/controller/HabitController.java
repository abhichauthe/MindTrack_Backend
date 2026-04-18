package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.HabitDto;
import com.Mindwork.mindtrack.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    // Create a new habit for a user
    @PostMapping
    public ResponseEntity<?> createHabit(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody HabitDto.CreateHabitRequest request) {
        try {
            HabitDto.HabitResponse habit = habitService.createHabit(userId, request);
            return ResponseEntity.ok(habit);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Get all habits for a user
    @GetMapping
    public ResponseEntity<List<HabitDto.HabitResponse>> getHabits(
            @RequestHeader("X-User-Id") Long userId) {
        List<HabitDto.HabitResponse> habits = habitService.getHabitsByUser(userId);
        return ResponseEntity.ok(habits);
    }

    // Log a habit (mark as done/not done)
    @PostMapping("/{id}/log")
    public ResponseEntity<?> logHabit(
            @PathVariable Long id,
            @RequestBody(required = false) HabitDto.LogHabitRequest request) {
        try {
            if (request == null) request = new HabitDto.LogHabitRequest();
            HabitDto.HabitLogResponse log = habitService.logHabit(id, request);
            return ResponseEntity.ok(log);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String error) {}
}
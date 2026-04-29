package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.RecurringHabitDto;
import com.Mindwork.mindtrack.service.RecurringHabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-habits")
@RequiredArgsConstructor
public class RecurringHabitController {

    private final RecurringHabitService recurringHabitService;

    // GET all recurring habits for user
    @GetMapping
    public ResponseEntity<List<RecurringHabitDto.RecurringHabitResponse>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(recurringHabitService.getAll(userId));
    }

    // POST create a recurring habit
    @PostMapping
    public ResponseEntity<RecurringHabitDto.RecurringHabitResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RecurringHabitDto.CreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurringHabitService.create(userId, request));
    }

    // PUT update recurring habit (propagates to all future blocks)
    @PutMapping("/{id}")
    public ResponseEntity<RecurringHabitDto.RecurringHabitResponse> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RecurringHabitDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(recurringHabitService.update(userId, id, request));
    }

    // DELETE deactivate (stops future generation, removes pending blocks)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        recurringHabitService.deactivate(userId, id);
        return ResponseEntity.noContent().build();
    }
}
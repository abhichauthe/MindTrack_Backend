package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.CheckInDto;
import com.Mindwork.mindtrack.dto.DisciplineDto;
import com.Mindwork.mindtrack.service.DisciplineCheckInService;
import com.Mindwork.mindtrack.service.DisciplineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discipline")
@RequiredArgsConstructor
public class DisciplineController {

    private final DisciplineService        disciplineService;
    private final DisciplineCheckInService checkInService;

    // ── Discipline CRUD ───────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<DisciplineDto.DisciplineResponse>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(disciplineService.getAll(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<DisciplineDto.DisciplineResponse>> getActive(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(disciplineService.getActive(userId));
    }

    @PostMapping
    public ResponseEntity<DisciplineDto.DisciplineResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody DisciplineDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disciplineService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisciplineDto.DisciplineResponse> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody DisciplineDto.UpdateRequest request) {
        return ResponseEntity.ok(disciplineService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        disciplineService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ── Check-in endpoints ────────────────────────────────────────────

    // POST /api/discipline/{id}/checkin — submit daily check-in
    @PostMapping("/{id}/checkin")
    public ResponseEntity<?> submitCheckIn(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody CheckInDto.CheckInRequest request) {
        try {
            return ResponseEntity.ok(checkInService.submitCheckIn(userId, id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET /api/discipline/{id}/checkins — get check-in history + summary
    @GetMapping("/{id}/checkins")
    public ResponseEntity<?> getCheckIns(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(checkInService.getCheckIns(userId, id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String error) {}
}
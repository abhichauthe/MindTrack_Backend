package com.Mindwork.mindtrack.controller;



import com.Mindwork.mindtrack.dto.FocusDto;
import com.Mindwork.mindtrack.service.FocusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/focus")
@RequiredArgsConstructor
public class FocusController {

    private final FocusService focusService;

    // Save a completed/cancelled session
    @PostMapping("/sessions")
    public ResponseEntity<?> saveSession(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody FocusDto.SaveSessionRequest request) {
        try {
            return ResponseEntity.ok(focusService.saveSession(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Get all sessions for history
    @GetMapping("/sessions")
    public ResponseEntity<List<FocusDto.FocusSessionResponse>> getSessions(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(focusService.getSessions(userId));
    }

    // Get focus stats (today + week)
    @GetMapping("/stats")
    public ResponseEntity<FocusDto.FocusStatsResponse> getStats(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(focusService.getStats(userId));
    }

    record ErrorResponse(String error) {}
}
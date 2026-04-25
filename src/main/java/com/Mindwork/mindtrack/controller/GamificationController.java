package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.GamificationDto;
import com.Mindwork.mindtrack.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    // Get full stats — XP, level, badges, streaks
    @GetMapping("/stats")
    public ResponseEntity<GamificationDto.UserStatsResponse> getStats(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(gamificationService.getStats(userId));
    }
}
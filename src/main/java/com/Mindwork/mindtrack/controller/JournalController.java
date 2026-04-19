package com.Mindwork.mindtrack.controller;

import com.Mindwork.mindtrack.dto.JournalDto;
import com.Mindwork.mindtrack.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    public ResponseEntity<?> createEntry(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody JournalDto.CreateEntryRequest request) {
        try {
            return ResponseEntity.ok(journalService.createEntry(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<JournalDto.JournalEntryResponse>> getEntries(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(journalService.getEntries(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEntry(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(journalService.getEntry(userId, id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody JournalDto.UpdateEntryRequest request) {
        try {
            return ResponseEntity.ok(journalService.updateEntry(userId, id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        try {
            journalService.deleteEntry(userId, id);
            return ResponseEntity.ok(new ErrorResponse("Entry deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
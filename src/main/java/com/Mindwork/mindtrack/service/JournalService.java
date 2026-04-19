package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.JournalDto;
import com.Mindwork.mindtrack.entity.JournalEntry;
import com.Mindwork.mindtrack.entity.Notification;
import com.Mindwork.mindtrack.entity.User;
import com.Mindwork.mindtrack.repository.JournalEntryRepository;
import com.Mindwork.mindtrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final UserRepository         userRepository;
    private final NotificationService    notificationService; // ← added

    public JournalDto.JournalEntryResponse createEntry(Long userId, JournalDto.CreateEntryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JournalEntry entry = JournalEntry.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood() != null ? request.getMood() : JournalEntry.Mood.NEUTRAL)
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .build();

        JournalEntry saved = journalEntryRepository.save(entry);

        // ── Send notification when journal entry is created ───────────
        notificationService.createNotification(
                userId,
                "Journal entry saved! ◫",
                "Your entry \"" + saved.getTitle() + "\" has been recorded successfully.",
                Notification.NotificationType.JOURNAL_REMINDER
        );

        return new JournalDto.JournalEntryResponse(saved);
    }

    public List<JournalDto.JournalEntryResponse> getEntries(Long userId) {
        return journalEntryRepository.findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(JournalDto.JournalEntryResponse::new)
                .collect(Collectors.toList());
    }

    public JournalDto.JournalEntryResponse getEntry(Long userId, Long entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return new JournalDto.JournalEntryResponse(entry);
    }

    public JournalDto.JournalEntryResponse updateEntry(Long userId, Long entryId, JournalDto.UpdateEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.getTitle()   != null) entry.setTitle(request.getTitle());
        if (request.getContent() != null) entry.setContent(request.getContent());
        if (request.getMood()    != null) entry.setMood(request.getMood());

        return new JournalDto.JournalEntryResponse(journalEntryRepository.save(entry));
    }

    public void deleteEntry(Long userId, Long entryId) {
        JournalEntry entry = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        if (!entry.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        journalEntryRepository.delete(entry);
    }
}
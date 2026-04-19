package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List< JournalEntry> findByUserIdOrderByDateDesc(Long userId);

    Optional<JournalEntry> findByUserIdAndDate(Long userId, LocalDate date);

    List<JournalEntry> findByUserIdAndMood(Long userId, JournalEntry.Mood mood);
}
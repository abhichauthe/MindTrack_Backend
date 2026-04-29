package com.Mindwork.mindtrack.scheduler;

import com.Mindwork.mindtrack.service.RecurringHabitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurringHabitScheduler {

    private final RecurringHabitService recurringHabitService;

    // ── Runs every day at midnight ─────────────────────────────────
    // Auto-generates timetable blocks for all active recurring habits
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyBlocks() {
        LocalDate today = LocalDate.now();
        log.info("Generating recurring blocks for {}", today);
        recurringHabitService.generateBlocksForDate(today);
    }

    // ── Also generates blocks for today on app startup ─────────────
    @EventListener(ApplicationReadyEvent.class)
    public void generateTodayOnStartup() {
        LocalDate today = LocalDate.now();
        log.info("Startup: generating recurring blocks for {}", today);
        recurringHabitService.generateBlocksForDate(today);
    }
}
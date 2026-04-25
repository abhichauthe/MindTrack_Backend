package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.GamificationDto;
import com.Mindwork.mindtrack.entity.*;
import com.Mindwork.mindtrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    // ── XP values per action ──────────────────────────────────────────────
    private static final int XP_HABIT_COMPLETE  = 10;
    private static final int XP_FOCUS_SESSION   = 15;
    private static final int XP_JOURNAL_ENTRY   = 10;
    private static final int XP_STREAK_BONUS_7  = 25;
    private static final int XP_STREAK_BONUS_30 = 100;

    private final UserStatsRepository  userStatsRepository;
    private final StreakRepository     streakRepository;
    private final BadgeRepository      badgeRepository;
    private final UserRepository       userRepository;
    private final NotificationService  notificationService;

    // ── Called when habit is marked DONE ─────────────────────────────────
    public void onHabitCompleted(Long userId, Habit habit) {
        UserStats stats = getOrCreateStats(userId);
        int oldLevel    = stats.getLevel();

        // 1. Update streak
        Streak streak = updateStreak(userId, habit);

        // 2. Award base XP + bonus for milestone streaks
        int xpGained = XP_HABIT_COMPLETE;
        if (streak.getCurrentStreak() == 7)  xpGained += XP_STREAK_BONUS_7;
        if (streak.getCurrentStreak() == 30) xpGained += XP_STREAK_BONUS_30;

        stats.addXp(xpGained);
        stats.setTotalHabitsCompleted(stats.getTotalHabitsCompleted() + 1);
        userStatsRepository.save(stats);

        // 3. Check badges
        checkAndAwardStreakBadges(userId, streak.getCurrentStreak());
        checkAndAwardFirstBadge(userId, Badge.BadgeType.FIRST_HABIT,
                "First habit completed! ✅",
                "You completed your very first habit. The journey begins!");

        // 4. Check level up
        if (stats.getLevel() > oldLevel) {
            onLevelUp(userId, stats.getLevel());
        }

        // 5. Fire XP notification
        notificationService.createNotification(
                userId,
                "+" + xpGained + " XP earned! 🔥",
                "Habit \"" + habit.getName() + "\" completed. "
                        + "Streak: " + streak.getCurrentStreak() + " days.",
                Notification.NotificationType.HABIT_REMINDER
        );
    }

    // ── Called when focus session is COMPLETED ────────────────────────────
    public void onFocusCompleted(Long userId, FocusSession session) {
        UserStats stats = getOrCreateStats(userId);
        int oldLevel    = stats.getLevel();

        stats.addXp(XP_FOCUS_SESSION);
        stats.setTotalFocusMinutes(
                stats.getTotalFocusMinutes() + session.getDurationMinutes()
        );
        userStatsRepository.save(stats);

        // First focus badge
        checkAndAwardFirstBadge(userId, Badge.BadgeType.FIRST_FOCUS,
                "First focus session! 🎯",
                "You completed your first focus session. Deep work unlocked!");

        // Focus master — 60+ total focus minutes
        if (stats.getTotalFocusMinutes() >= 60
                && !badgeRepository.existsByUserIdAndType(
                userId, Badge.BadgeType.FOCUS_MASTER)) {
            awardBadge(userId, Badge.BadgeType.FOCUS_MASTER,
                    "Focus Master unlocked! 🧠",
                    "You've focused for 60+ minutes total. You're in the zone!");
        }

        if (stats.getLevel() > oldLevel) {
            onLevelUp(userId, stats.getLevel());
        }

        notificationService.createNotification(
                userId,
                "+" + XP_FOCUS_SESSION + " XP earned! 🎯",
                "Focus session of " + session.getDurationMinutes()
                        + " min completed. Total XP: " + stats.getTotalXp(),
                Notification.NotificationType.FOCUS_COMPLETE
        );
    }

    // ── Called when journal entry is CREATED ──────────────────────────────
    public void onJournalCreated(Long userId) {
        UserStats stats = getOrCreateStats(userId);
        int oldLevel    = stats.getLevel();

        stats.addXp(XP_JOURNAL_ENTRY);
        stats.setTotalJournalEntries(stats.getTotalJournalEntries() + 1);
        userStatsRepository.save(stats);

        // First journal badge
        checkAndAwardFirstBadge(userId, Badge.BadgeType.FIRST_JOURNAL,
                "First journal entry! 📔",
                "You wrote your first journal entry. Self-reflection begins!");

        // 7 journal entries badge
        if (stats.getTotalJournalEntries() >= 7
                && !badgeRepository.existsByUserIdAndType(
                userId, Badge.BadgeType.JOURNAL_STREAK_7)) {
            awardBadge(userId, Badge.BadgeType.JOURNAL_STREAK_7,
                    "Reflective Mind unlocked! 🪞",
                    "You've written 7 journal entries. Keep reflecting!");
        }

        if (stats.getLevel() > oldLevel) {
            onLevelUp(userId, stats.getLevel());
        }

        notificationService.createNotification(
                userId,
                "+" + XP_JOURNAL_ENTRY + " XP earned! 📔",
                "Journal entry saved. Total XP: " + stats.getTotalXp(),
                Notification.NotificationType.JOURNAL_REMINDER
        );
    }

    // ── Get full stats for dashboard ──────────────────────────────────────
    public GamificationDto.UserStatsResponse getStats(Long userId) {
        UserStats stats = getOrCreateStats(userId);

        List<GamificationDto.BadgeResponse> badges = badgeRepository
                .findByUserIdOrderByEarnedAtDesc(userId)
                .stream()
                .map(GamificationDto.BadgeResponse::new)
                .collect(Collectors.toList());

        List<GamificationDto.StreakResponse> streaks = streakRepository
                .findByUserIdOrderByCurrentStreakDesc(userId)
                .stream()
                .map(s -> new GamificationDto.StreakResponse(
                        s.getHabit().getId(),
                        s.getHabit().getName(),
                        s.getCurrentStreak(),
                        s.getLongestStreak(),
                        s.getLastCompletedDate()
                ))
                .collect(Collectors.toList());

        return new GamificationDto.UserStatsResponse(
                stats.getTotalXp(),
                stats.getLevel(),
                stats.getTotalHabitsCompleted(),
                stats.getTotalFocusMinutes(),
                stats.getTotalJournalEntries(),
                badges,
                streaks
        );
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    private UserStats getOrCreateStats(Long userId) {
        return userStatsRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return userStatsRepository.save(
                    UserStats.builder().user(user).build()
            );
        });
    }

    private Streak updateStreak(Long userId, Habit habit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Streak streak = streakRepository
                .findByUserIdAndHabitId(userId, habit.getId())
                .orElseGet(() -> Streak.builder()
                        .user(user)
                        .habit(habit)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build());

        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        if (streak.getLastCompletedDate() == null) {
            // First ever completion
            streak.setCurrentStreak(1);

        } else if (streak.getLastCompletedDate().equals(today)) {
            // Already logged today — no change
            return streak;

        } else if (streak.getLastCompletedDate().equals(yesterday)) {
            // Consecutive day — extend streak
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);

        } else {
            // Gap detected — reset streak
            streak.setCurrentStreak(1);
        }

        streak.setLastCompletedDate(today);

        // Update longest streak record
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        return streakRepository.save(streak);
    }

    private void checkAndAwardStreakBadges(Long userId, int currentStreak) {
        if (currentStreak >= 3) {
            checkAndAwardFirstBadge(userId, Badge.BadgeType.STREAK_3,
                    "3-Day Streak! 🔥",
                    "You completed a habit 3 days in a row. You're on fire!");
        }
        if (currentStreak >= 7) {
            checkAndAwardFirstBadge(userId, Badge.BadgeType.STREAK_7,
                    "Week Warrior! ⚡",
                    "7-day streak achieved. Consistency is your superpower!");
        }
        if (currentStreak >= 30) {
            checkAndAwardFirstBadge(userId, Badge.BadgeType.STREAK_30,
                    "Monthly Master! 🏆",
                    "30-day streak! You've built a real habit. Incredible!");
        }
        if (currentStreak >= 100) {
            checkAndAwardFirstBadge(userId, Badge.BadgeType.STREAK_100,
                    "Century Club! 💎",
                    "100-day streak! You are absolutely unstoppable!");
        }
    }

    private void checkAndAwardFirstBadge(Long userId, Badge.BadgeType type,
                                         String notifTitle, String notifMessage) {
        if (!badgeRepository.existsByUserIdAndType(userId, type)) {
            awardBadge(userId, type, notifTitle, notifMessage);
        }
    }

    private void awardBadge(Long userId, Badge.BadgeType type,
                            String notifTitle, String notifMessage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        badgeRepository.save(
                Badge.builder().user(user).type(type).build()
        );

        notificationService.createNotification(
                userId,
                notifTitle,
                notifMessage,
                Notification.NotificationType.SYSTEM
        );
    }

    private void onLevelUp(Long userId, int newLevel) {
        Badge.BadgeType levelBadge = switch (newLevel) {
            case 5  -> Badge.BadgeType.LEVEL_5;
            case 10 -> Badge.BadgeType.LEVEL_10;
            case 20 -> Badge.BadgeType.LEVEL_20;
            default -> null;
        };

        if (levelBadge != null) {
            checkAndAwardFirstBadge(userId, levelBadge,
                    "Level " + newLevel + " reached! 🌟",
                    "You leveled up to Level " + newLevel + ". Keep going!");
        } else {
            notificationService.createNotification(
                    userId,
                    "Level Up! 🌟 You are now Level " + newLevel,
                    "Keep completing habits to reach the next level!",
                    Notification.NotificationType.SYSTEM
            );
        }
    }
}
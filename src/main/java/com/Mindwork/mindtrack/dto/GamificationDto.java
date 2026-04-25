package com.Mindwork.mindtrack.dto;

import com.Mindwork.mindtrack.entity.Badge;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class GamificationDto {

    @Data
    public static class UserStatsResponse {
        private Integer totalXp;
        private Integer level;
        private Integer xpToNextLevel;
        private Integer xpProgressPercent;
        private Integer totalHabitsCompleted;
        private Integer totalFocusMinutes;
        private Integer totalJournalEntries;
        private List<BadgeResponse> badges;
        private List<StreakResponse> streaks;

        public UserStatsResponse(Integer totalXp, Integer level,
                                 Integer totalHabitsCompleted,
                                 Integer totalFocusMinutes,
                                 Integer totalJournalEntries,
                                 List<BadgeResponse> badges,
                                 List<StreakResponse> streaks) {
            this.totalXp               = totalXp;
            this.level                 = level;
            this.xpToNextLevel         = (level * 100) - totalXp;
            this.xpProgressPercent     = totalXp % 100;
            this.totalHabitsCompleted  = totalHabitsCompleted;
            this.totalFocusMinutes     = totalFocusMinutes;
            this.totalJournalEntries   = totalJournalEntries;
            this.badges                = badges;
            this.streaks               = streaks;
        }
    }

    @Data
    public static class StreakResponse {
        private Long habitId;
        private String habitName;
        private Integer currentStreak;
        private Integer longestStreak;
        private LocalDate lastCompletedDate;

        public StreakResponse(Long habitId, String habitName,
                              Integer currentStreak, Integer longestStreak,
                              LocalDate lastCompletedDate) {
            this.habitId           = habitId;
            this.habitName         = habitName;
            this.currentStreak     = currentStreak;
            this.longestStreak     = longestStreak;
            this.lastCompletedDate = lastCompletedDate;
        }
    }

    @Data
    public static class BadgeResponse {
        private Badge.BadgeType type;
        private String label;
        private String description;
        private String icon;
        private LocalDateTime earnedAt;

        public BadgeResponse(Badge badge) {
            this.type        = badge.getType();
            this.label       = getLabel(badge.getType());
            this.description = getDescription(badge.getType());
            this.icon        = getIcon(badge.getType());
            this.earnedAt    = badge.getEarnedAt();
        }

        private String getLabel(Badge.BadgeType type) {
            return switch (type) {
                case STREAK_3        -> "3-Day Streak";
                case STREAK_7        -> "Week Warrior";
                case STREAK_30       -> "Monthly Master";
                case STREAK_100      -> "Century Club";
                case LEVEL_5         -> "Level 5 Achiever";
                case LEVEL_10        -> "Level 10 Champion";
                case LEVEL_20        -> "Level 20 Legend";
                case FIRST_HABIT     -> "First Step";
                case FIRST_FOCUS     -> "Focus Initiated";
                case FIRST_JOURNAL   -> "Inner Voice";
                case FOCUS_MASTER    -> "Focus Master";
                case JOURNAL_STREAK_7-> "Reflective Mind";
            };
        }

        private String getDescription(Badge.BadgeType type) {
            return switch (type) {
                case STREAK_3        -> "Completed a habit 3 days in a row";
                case STREAK_7        -> "Completed a habit 7 days in a row";
                case STREAK_30       -> "Completed a habit 30 days in a row";
                case STREAK_100      -> "Completed a habit 100 days in a row";
                case LEVEL_5         -> "Reached Level 5";
                case LEVEL_10        -> "Reached Level 10";
                case LEVEL_20        -> "Reached Level 20";
                case FIRST_HABIT     -> "Completed your first habit";
                case FIRST_FOCUS     -> "Completed your first focus session";
                case FIRST_JOURNAL   -> "Wrote your first journal entry";
                case FOCUS_MASTER    -> "Focused for 60+ minutes in one day";
                case JOURNAL_STREAK_7-> "Wrote 7 journal entries";
            };
        }

        private String getIcon(Badge.BadgeType type) {
            return switch (type) {
                case STREAK_3        -> "🔥";
                case STREAK_7        -> "⚡";
                case STREAK_30       -> "🏆";
                case STREAK_100      -> "💎";
                case LEVEL_5         -> "⭐";
                case LEVEL_10        -> "🌟";
                case LEVEL_20        -> "👑";
                case FIRST_HABIT     -> "✅";
                case FIRST_FOCUS     -> "🎯";
                case FIRST_JOURNAL   -> "📔";
                case FOCUS_MASTER    -> "🧠";
                case JOURNAL_STREAK_7-> "🪞";
            };
        }
    }

    @Data
    public static class XpGainResponse {
        private String action;
        private Integer xpGained;
        private Integer totalXp;
        private Integer level;
        private boolean leveledUp;
        private Badge.BadgeType newBadge;

        public XpGainResponse(String action, Integer xpGained,
                              Integer totalXp, Integer level,
                              boolean leveledUp, Badge.BadgeType newBadge) {
            this.action    = action;
            this.xpGained  = xpGained;
            this.totalXp   = totalXp;
            this.level     = level;
            this.leveledUp = leveledUp;
            this.newBadge  = newBadge;
        }
    }
}
package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    // ─────────────────────────────────────────────────────────────
    // 1. BASIC FETCH METHODS
    // ─────────────────────────────────────────────────────────────

    // Get all blocks for a user on a specific date (sorted)
    List<TimeBlock> findByUserIdAndDateOrderByStartTimeAsc(Long userId, LocalDate date);

    // Get all blocks in a date range (useful for weekly/monthly view)
    List<TimeBlock> findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Get blocks by category (Work / Health / Discipline etc.)
    List<TimeBlock> findByUserIdAndDateAndCategoryOrderByStartTimeAsc(
            Long userId,
            LocalDate date,
            TimeBlock.BlockCategory category
    );

    // ─────────────────────────────────────────────────────────────
    // 2. COUNT / ANALYTICS METHODS
    // ─────────────────────────────────────────────────────────────

    // Count completed tasks
    long countByUserIdAndDateAndStatus(
            Long userId,
            LocalDate date,
            TimeBlock.BlockStatus status
    );

    // Count total tasks
    long countByUserIdAndDate(Long userId, LocalDate date);

    // ─────────────────────────────────────────────────────────────
    // 3. CONFLICT DETECTION (MOST IMPORTANT)
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT b FROM TimeBlock b WHERE b.user.id = :userId " +
            "AND b.date = :date " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    List<TimeBlock> findConflictingBlocks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // ─────────────────────────────────────────────────────────────
    // 4. OVERLAPPING CHECK (FOR UPDATE CASE)
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT t FROM TimeBlock t WHERE t.user.id = :userId " +
            "AND t.date = :date " +
            "AND t.id != :excludeId " +
            "AND t.startTime < :endTime " +
            "AND t.endTime > :startTime")
    List<TimeBlock> findOverlappingBlocks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    // ─────────────────────────────────────────────────────────────
    // 5. CURRENT ACTIVE TASKS (REAL-TIME CHECK)
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT t FROM TimeBlock t WHERE t.user.id = :userId " +
            "AND t.date = :date " +
            "AND t.startTime <= :currentTime " +
            "AND t.endTime > :currentTime")
    List<TimeBlock> findActiveBlocks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("currentTime") LocalTime currentTime
    );
}

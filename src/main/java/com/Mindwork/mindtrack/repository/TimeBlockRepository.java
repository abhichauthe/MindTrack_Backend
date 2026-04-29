package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    // ── BASIC FETCH ────────────────────────────────────────

    List<TimeBlock> findByUserIdAndDateOrderByStartTimeAsc(
            Long userId, LocalDate date
    );

    List<TimeBlock> findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<TimeBlock> findByUserIdAndDateAndCategoryOrderByStartTimeAsc(
            Long userId,
            LocalDate date,
            TimeBlock.BlockCategory category
    );

    // ── ANALYTICS ─────────────────────────────────────────

    long countByUserIdAndDateAndStatus(
            Long userId,
            LocalDate date,
            TimeBlock.BlockStatus status
    );

    long countByUserIdAndDate(Long userId, LocalDate date);

    // ── CONFLICT DETECTION ────────────────────────────────

    @Query("""
        SELECT b FROM TimeBlock b
        WHERE b.user.id = :userId
        AND b.date = :date
        AND b.startTime < :endTime
        AND b.endTime > :startTime
    """)
    List<TimeBlock> findConflictingBlocks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // ── OVERLAP CHECK ─────────────────────────────────────

    @Query("""
        SELECT t FROM TimeBlock t
        WHERE t.user.id = :userId
        AND t.date = :date
        AND t.id != :excludeId
        AND t.startTime < :endTime
        AND t.endTime > :startTime
    """)
    List<TimeBlock> findOverlappingBlocks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );

    // ── ACTIVE BLOCKS ─────────────────────────────────────

    @Query("""
        SELECT t FROM TimeBlock t
        WHERE t.user.id = :userId
        AND t.date = :date
        AND t.startTime <= :currentTime
        AND t.endTime > :currentTime
    """)
    List<TimeBlock> findActiveBlocks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("currentTime") LocalTime currentTime
    );

    // ── RECURRING SUPPORT ─────────────────────────────────

    boolean existsByRecurringHabitIdAndDate(
            Long recurringHabitId,
            LocalDate date
    );

    @Query("""
        SELECT b FROM TimeBlock b
        WHERE b.recurringHabit.id = :habitId
        AND b.date >= :fromDate
        ORDER BY b.date ASC
    """)
    List<TimeBlock> findFutureBlocksByRecurringHabitId(
            @Param("habitId") Long habitId,
            @Param("fromDate") LocalDate fromDate
    );
}
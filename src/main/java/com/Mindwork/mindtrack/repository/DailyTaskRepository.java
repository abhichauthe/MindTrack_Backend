package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {

    // ─────────────────────────────────────────────────────────────
    // Basic Fetch Methods
    // ─────────────────────────────────────────────────────────────

    List<DailyTask> findByUserIdAndDueDateOrderByPriorityAscDueTimeAsc(
            Long userId, LocalDate date
    );

    List<DailyTask> findByWeeklyBreakdownIdOrderByDueDateAscPriorityAsc(
            Long weekId
    );

    List<DailyTask> findByUserIdAndDueDateBetweenOrderByDueDateAscPriorityAsc(
            Long userId, LocalDate start, LocalDate end
    );

    // ─────────────────────────────────────────────────────────────
    // Analytics
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(t) FROM DailyTask t WHERE t.user.id = :userId " +
            "AND t.dueDate = :date AND t.status = 'DONE'")
    long countCompletedByDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    @Query("SELECT COUNT(t) FROM DailyTask t WHERE t.user.id = :userId " +
            "AND t.dueDate = :date")
    long countTotalByDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    // ─────────────────────────────────────────────────────────────
    // Scheduler Support
    // ─────────────────────────────────────────────────────────────

    // Tasks scheduled for today (USER specific)
    @Query("SELECT t FROM DailyTask t WHERE t.user.id = :userId " +
            "AND t.dueDate = :date " +
            "AND t.dueTime IS NOT NULL " +
            "AND t.status = 'PENDING'")
    List<DailyTask> findScheduledTasksForToday(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    // Tasks scheduled for today (GLOBAL - used in scheduler)
    @Query("SELECT t FROM DailyTask t WHERE t.dueDate = :date " +
            "AND t.dueTime IS NOT NULL " +
            "AND t.status = 'PENDING'")
    List<DailyTask> findScheduledTasksForToday(
            @Param("date") LocalDate date
    );

    // ─────────────────────────────────────────────────────────────
    // Auto Scheduling
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT t FROM DailyTask t WHERE t.user.id = :userId " +
            "AND t.dueDate = :date " +
            "AND t.syncedToTimetable = false " +
            "AND t.status = 'PENDING' " +
            "ORDER BY CASE " +
            "WHEN t.priority = 'HIGH' THEN 1 " +
            "WHEN t.priority = 'MEDIUM' THEN 2 " +
            "ELSE 3 END")
    List<DailyTask> findUnscheduledTasksForDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    // ─────────────────────────────────────────────────────────────
    // Missed Tasks
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT t FROM DailyTask t WHERE t.user.id = :userId " +
            "AND t.dueDate = :date " +
            "AND t.status = 'PENDING'")
    List<DailyTask> findMissedTasks(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    // ─────────────────────────────────────────────────────────────
    // Active Tasks (IMPORTANT FIX)
    // ─────────────────────────────────────────────────────────────

    @Query("SELECT t FROM DailyTask t WHERE t.dueDate = :date " +
            "AND t.dueTime IS NOT NULL " +
            "AND t.dueTime <= :currentTime " +
            "AND t.status = 'PENDING'")
    List<DailyTask> findCurrentlyActiveTasks(
            @Param("date") LocalDate date,
            @Param("currentTime") LocalTime currentTime
    );
}

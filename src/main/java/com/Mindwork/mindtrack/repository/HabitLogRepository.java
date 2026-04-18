package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.Habit;
import com.Mindwork.mindtrack.entity.HabitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByHabit(Habit habit);
    List<HabitLog> findByHabitAndDate(Habit habit, LocalDate date);
    Optional<HabitLog> findByHabitAndDateAndStatus(Habit habit, LocalDate date, HabitLog.LogStatus status);
    boolean existsByHabitAndDate(Habit habit, LocalDate date);
}
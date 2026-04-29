package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.RecurringHabit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringHabitRepository
        extends JpaRepository<RecurringHabit, Long> {

    // All active recurring habits for a user
    List<RecurringHabit> findByUserIdAndActiveTrue(Long userId);

    // All active habits that should generate blocks for a given date
    @Query("""
        SELECT r FROM RecurringHabit r
        WHERE r.active = true
        AND r.startDate <= :date
        AND (r.endDate IS NULL OR r.endDate >= :date)
    """)
    List<RecurringHabit> findAllActiveForDate(@Param("date") LocalDate date);

    // Active habits for a specific user on a given date
    @Query("""
        SELECT r FROM RecurringHabit r
        WHERE r.user.id = :userId
        AND r.active = true
        AND r.startDate <= :date
        AND (r.endDate IS NULL OR r.endDate >= :date)
    """)
    List<RecurringHabit> findByUserIdAndActiveForDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );
}
package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.WeeklyBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyBreakdownRepository extends JpaRepository<WeeklyBreakdown, Long> {
    List<WeeklyBreakdown> findByMonthlyPlanIdOrderByWeekNumber(Long planId);
    List<WeeklyBreakdown> findByUserIdOrderByStartDateDesc(Long userId);
}
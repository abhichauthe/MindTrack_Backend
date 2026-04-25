package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.MonthlyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyPlanRepository extends JpaRepository<MonthlyPlan, Long> {
    List<MonthlyPlan> findByUserIdOrderByYearDescMonthDesc(Long userId);
    Optional<MonthlyPlan> findByUserIdAndMonthAndYear(Long userId, int month, int year);
    List<MonthlyPlan> findByUserIdAndStatus(Long userId, MonthlyPlan.PlanStatus status);
}
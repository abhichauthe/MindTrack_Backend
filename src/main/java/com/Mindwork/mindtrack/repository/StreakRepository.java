package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreakRepository extends JpaRepository<Streak, Long> {
    Optional<Streak> findByUserIdAndHabitId(Long userId, Long habitId);
    List<Streak> findByUserIdOrderByCurrentStreakDesc(Long userId);
}
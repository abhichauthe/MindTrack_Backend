package com.Mindwork.mindtrack.repository;


import com.Mindwork.mindtrack.entity.Habit;
import com.Mindwork.mindtrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUser(User user);
    List<Habit> findByUserId(Long userId);
}
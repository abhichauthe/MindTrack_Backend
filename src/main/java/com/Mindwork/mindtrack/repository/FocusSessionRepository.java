package com.Mindwork.mindtrack.repository;


import com.Mindwork.mindtrack.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    List<FocusSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<FocusSession> findByUserIdAndStatus(Long userId, FocusSession.SessionStatus status);

    @Query("SELECT COALESCE(SUM(f.durationMinutes), 0) FROM FocusSession f " +
            "WHERE f.user.id = :userId AND f.status = 'COMPLETED' " +
            "AND f.completedAt >= :since")
    Integer getTotalFocusMinutesSince(Long userId, LocalDateTime since);

    long countByUserIdAndStatus(Long userId, FocusSession.SessionStatus status);
}
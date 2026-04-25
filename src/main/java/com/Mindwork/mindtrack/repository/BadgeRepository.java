package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserIdOrderByEarnedAtDesc(Long userId);
    Optional<Badge> findByUserIdAndType(Long userId, Badge.BadgeType type);
    boolean existsByUserIdAndType(Long userId, Badge.BadgeType type);
}
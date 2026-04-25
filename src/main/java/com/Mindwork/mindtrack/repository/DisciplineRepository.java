package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

    List<Discipline> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Discipline> findByUserIdAndStatus(Long userId, Discipline.DisciplineStatus status);

    long countByUserIdAndStatus(Long userId, Discipline.DisciplineStatus status);
}
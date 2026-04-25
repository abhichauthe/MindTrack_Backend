package com.Mindwork.mindtrack.repository;

import com.Mindwork.mindtrack.entity.DisciplineCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisciplineCheckInRepository extends JpaRepository<DisciplineCheckIn, Long> {

    List<DisciplineCheckIn> findByDisciplineIdOrderByDateDesc(Long disciplineId);

    Optional<DisciplineCheckIn> findByDisciplineIdAndDate(Long disciplineId, LocalDate date);

    boolean existsByDisciplineIdAndDate(Long disciplineId, LocalDate date);

    long countByDisciplineId(Long disciplineId);
}
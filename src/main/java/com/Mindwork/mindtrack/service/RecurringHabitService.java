package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.RecurringHabitDto;
import com.Mindwork.mindtrack.entity.*;
import com.Mindwork.mindtrack.exception.BadRequestException;
import com.Mindwork.mindtrack.exception.ForbiddenException;
import com.Mindwork.mindtrack.exception.NotFoundException;
import com.Mindwork.mindtrack.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringHabitService {

    private final RecurringHabitRepository recurringHabitRepository;
    private final TimeBlockRepository timeBlockRepository;
    private final UserRepository userRepository;

    // ── CREATE ─────────────────────────────────────────────
    public RecurringHabitDto.RecurringHabitResponse create(
            Long userId,
            RecurringHabitDto.CreateRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateTimeRange(req.getStartTime(), req.getEndTime());
        validateDates(req.getStartDate() != null ? req.getStartDate() : LocalDate.now(), req.getEndDate());

        RecurringHabit rh = RecurringHabit.builder()
                .user(user)
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory() != null
                        ? req.getCategory()
                        : RecurringHabit.BlockCategory.HABIT)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .startDate(req.getStartDate() != null
                        ? req.getStartDate() : LocalDate.now())
                .endDate(req.getEndDate())
                .active(true)
                .build();

        RecurringHabit saved = recurringHabitRepository.save(rh);

        // Generate today's block if applicable
        if (!saved.getStartDate().isAfter(LocalDate.now())) {
            generateBlockForDate(saved, LocalDate.now());
        }

        return new RecurringHabitDto.RecurringHabitResponse(saved);
    }

    // ── GET ALL ────────────────────────────────────────────
    public List<RecurringHabitDto.RecurringHabitResponse> getAll(Long userId) {
        return recurringHabitRepository
                .findByUserIdAndActiveTrue(userId)
                .stream()
                .map(RecurringHabitDto.RecurringHabitResponse::new)
                .collect(Collectors.toList());
    }

    // ── UPDATE ─────────────────────────────────────────────
    public RecurringHabitDto.RecurringHabitResponse update(
            Long userId, Long id,
            RecurringHabitDto.UpdateRequest req) {

        RecurringHabit rh = getOwned(userId, id);

        if (req.getTitle() != null) rh.setTitle(req.getTitle());
        if (req.getDescription() != null) rh.setDescription(req.getDescription());
        if (req.getCategory() != null) rh.setCategory(req.getCategory());
        if (req.getEndDate() != null) rh.setEndDate(req.getEndDate());
        if (req.getActive() != null) rh.setActive(req.getActive());

        boolean timeChanged = false;

        if (req.getStartTime() != null) {
            rh.setStartTime(req.getStartTime());
            timeChanged = true;
        }
        if (req.getEndTime() != null) {
            rh.setEndTime(req.getEndTime());
            timeChanged = true;
        }

        if (timeChanged) {
            validateTimeRange(rh.getStartTime(), rh.getEndTime());
        }
        validateDates(rh.getStartDate(), rh.getEndDate());

        RecurringHabit saved = recurringHabitRepository.save(rh);

        if (timeChanged) {
            updateFutureTimetableBlocks(saved);
        }

        return new RecurringHabitDto.RecurringHabitResponse(saved);
    }

    // ── DEACTIVATE ─────────────────────────────────────────
    public void deactivate(Long userId, Long id) {
        RecurringHabit rh = getOwned(userId, id);
        rh.setActive(false);
        recurringHabitRepository.save(rh);

        removeFutureTimetableBlocks(rh);
    }

    // ── DAILY SCHEDULER ────────────────────────────────────
    public void generateBlocksForDate(LocalDate date) {
        List<RecurringHabit> habits =
                recurringHabitRepository.findAllActiveForDate(date);

        for (RecurringHabit rh : habits) {

            // Skip if past end date
            if (rh.getEndDate() != null && date.isAfter(rh.getEndDate())) {
                continue;
            }

            boolean exists = timeBlockRepository
                    .existsByRecurringHabitIdAndDate(rh.getId(), date);

            if (!exists) {
                generateBlockForDate(rh, date);
            }
        }
    }

    // ── GENERATE SINGLE BLOCK ──────────────────────────────
    private void generateBlockForDate(RecurringHabit rh, LocalDate date) {

        // Extra safety
        if (rh.getEndDate() != null && date.isAfter(rh.getEndDate())) return;

        TimeBlock block = TimeBlock.builder()
                .user(rh.getUser())
                .recurringHabit(rh)
                .title(rh.getTitle())
                .description(rh.getDescription())
                .category(mapCategory(rh.getCategory()))
                .startTime(rh.getStartTime())
                .endTime(rh.getEndTime())
                .date(date)
                .status(TimeBlock.BlockStatus.PENDING)
                .autoScheduled(true)
                .build();

        timeBlockRepository.save(block);
    }

    // ── UPDATE FUTURE BLOCKS ───────────────────────────────
    private void updateFutureTimetableBlocks(RecurringHabit rh) {

        List<TimeBlock> futureBlocks =
                timeBlockRepository.findFutureBlocksByRecurringHabitId(
                        rh.getId(), LocalDate.now()
                );

        futureBlocks.forEach(block -> {
            block.setStartTime(rh.getStartTime());
            block.setEndTime(rh.getEndTime());
            block.setTitle(rh.getTitle());
            block.setDescription(rh.getDescription());
            block.setCategory(mapCategory(rh.getCategory()));
        });

        timeBlockRepository.saveAll(futureBlocks);
    }

    // ── REMOVE FUTURE BLOCKS ───────────────────────────────
    private void removeFutureTimetableBlocks(RecurringHabit rh) {

        List<TimeBlock> future =
                timeBlockRepository.findFutureBlocksByRecurringHabitId(
                        rh.getId(), LocalDate.now()
                );

        List<TimeBlock> toDelete = future.stream()
                .filter(b -> b.getStatus() == TimeBlock.BlockStatus.PENDING)
                .toList();

        timeBlockRepository.deleteAll(toDelete);
    }

    // ── HELPERS ────────────────────────────────────────────
    private TimeBlock.BlockCategory mapCategory(
            RecurringHabit.BlockCategory cat) {

        return switch (cat) {
            case HABIT -> TimeBlock.BlockCategory.HABIT;
            case DISCIPLINE -> TimeBlock.BlockCategory.DISCIPLINE;
            case WORK -> TimeBlock.BlockCategory.WORK;
            case PERSONAL -> TimeBlock.BlockCategory.PERSONAL;
        };
    }

    private RecurringHabit getOwned(Long userId, Long id) {
        RecurringHabit rh = recurringHabitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recurring habit not found"));

        if (!rh.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Not allowed to access this resource");
        }

        return rh;
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BadRequestException("startTime and endTime are required");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("startTime must be before endTime");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BadRequestException("startDate is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("endDate must be on or after startDate");
        }
    }
}
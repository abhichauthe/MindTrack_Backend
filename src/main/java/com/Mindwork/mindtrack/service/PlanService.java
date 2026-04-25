package com.Mindwork.mindtrack.service;

import com.Mindwork.mindtrack.dto.PlanDto;
import com.Mindwork.mindtrack.entity.*;
import com.Mindwork.mindtrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final MonthlyPlanRepository      monthlyPlanRepository;
    private final WeeklyBreakdownRepository  weeklyBreakdownRepository;
    private final DailyTaskRepository        dailyTaskRepository;
    private final UserRepository             userRepository;
    private final DisciplineRepository       disciplineRepository;

    // ── Monthly Plan ──────────────────────────────────────────────────

    public PlanDto.MonthlyPlanResponse createPlan(Long userId,
                                                  PlanDto.CreatePlanRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Discipline discipline = null;
        if (request.getDisciplineId() != null) {
            discipline = disciplineRepository.findById(request.getDisciplineId())
                    .orElse(null);
        }

        MonthlyPlan plan = MonthlyPlan.builder()
                .user(user)
                .discipline(discipline)
                .title(request.getTitle())
                .description(request.getDescription())
                .month(request.getMonth())
                .year(request.getYear())
                .build();

        MonthlyPlan saved = monthlyPlanRepository.save(plan);

        if (request.isAutoGenerate()) {
            autoGenerateWeeks(saved, user, discipline);
        }

        return buildPlanResponse(saved);
    }

    public List<PlanDto.MonthlyPlanResponse> getAllPlans(Long userId) {
        return monthlyPlanRepository
                .findByUserIdOrderByYearDescMonthDesc(userId)
                .stream()
                .map(this::buildPlanResponse)
                .collect(Collectors.toList());
    }

    public PlanDto.MonthlyPlanResponse getPlan(Long userId, Long planId) {
        return buildPlanResponse(getOwnedPlan(userId, planId));
    }

    public void deletePlan(Long userId, Long planId) {
        monthlyPlanRepository.delete(getOwnedPlan(userId, planId));
    }

    // ── Weekly Breakdown ──────────────────────────────────────────────

    public List<PlanDto.WeeklyBreakdownResponse> getWeeks(Long userId, Long planId) {
        getOwnedPlan(userId, planId); // ownership check
        return weeklyBreakdownRepository
                .findByMonthlyPlanIdOrderByWeekNumber(planId)
                .stream()
                .map(this::buildWeekResponse)
                .collect(Collectors.toList());
    }

    // ── Daily Tasks ───────────────────────────────────────────────────

    public PlanDto.DailyTaskResponse createTask(Long userId,
                                                PlanDto.CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeeklyBreakdown week = null;
        if (request.getWeeklyBreakdownId() != null) {
            week = weeklyBreakdownRepository.findById(request.getWeeklyBreakdownId())
                    .orElse(null);
        }

        DailyTask task = DailyTask.builder()
                .user(user)
                .weeklyBreakdown(week)
                .title(request.getTitle())
                .description(request.getDescription())
                // ✅ FIXED: was TaskCategory.GENERAL which does not exist in the enum
                .category(request.getCategory() != null
                        ? request.getCategory()
                        : DailyTask.TaskCategory.OTHER)
                // ✅ Safe default for priority
                .priority(request.getPriority() != null
                        ? request.getPriority()
                        : DailyTask.TaskPriority.MEDIUM)
                .dueDate(request.getDueDate() != null
                        ? request.getDueDate()
                        : LocalDate.now())
                .dueTime(request.getDueTime())
                .durationMinutes(request.getDurationMinutes())
                .syncedToTimetable(false)
                .build();

        return new PlanDto.DailyTaskResponse(dailyTaskRepository.save(task));
    }

    public PlanDto.DailyTaskResponse updateTask(Long userId, Long taskId,
                                                PlanDto.UpdateTaskRequest request) {
        DailyTask task = getOwnedTask(userId, taskId);

        if (request.getTitle()           != null) task.setTitle(request.getTitle());
        if (request.getDescription()     != null) task.setDescription(request.getDescription());
        if (request.getCategory()        != null) task.setCategory(request.getCategory());
        if (request.getPriority()        != null) task.setPriority(request.getPriority());
        if (request.getDueDate()         != null) task.setDueDate(request.getDueDate());
        if (request.getDueTime()         != null) task.setDueTime(request.getDueTime());
        if (request.getDurationMinutes() != null) task.setDurationMinutes(request.getDurationMinutes());

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == DailyTask.TaskStatus.DONE
                    && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
            // Clear completedAt if task is moved out of DONE
            if (request.getStatus() != DailyTask.TaskStatus.DONE) {
                task.setCompletedAt(null);
            }
        }

        return new PlanDto.DailyTaskResponse(dailyTaskRepository.save(task));
    }

    public void deleteTask(Long userId, Long taskId) {
        dailyTaskRepository.delete(getOwnedTask(userId, taskId));
    }

    public PlanDto.TodaySummary getTodayTasks(Long userId) {
        LocalDate today = LocalDate.now();

        List<PlanDto.DailyTaskResponse> responses = dailyTaskRepository
                .findByUserIdAndDueDateOrderByPriorityAscDueTimeAsc(userId, today)
                .stream()
                .map(PlanDto.DailyTaskResponse::new)
                .collect(Collectors.toList());

        long total     = dailyTaskRepository.countTotalByDate(userId, today);
        long completed = dailyTaskRepository.countCompletedByDate(userId, today);

        return new PlanDto.TodaySummary(today, total, completed, responses);
    }

    public List<PlanDto.DailyTaskResponse> getTasksByWeek(Long userId, Long weekId) {
        return dailyTaskRepository
                .findByWeeklyBreakdownIdOrderByDueDateAscPriorityAsc(weekId)
                .stream()
                .map(PlanDto.DailyTaskResponse::new)
                .collect(Collectors.toList());
    }

    public List<PlanDto.DailyTaskResponse> getTasksByDateRange(Long userId,
                                                               LocalDate start,
                                                               LocalDate end) {
        return dailyTaskRepository
                .findByUserIdAndDueDateBetweenOrderByDueDateAscPriorityAsc(userId, start, end)
                .stream()
                .map(PlanDto.DailyTaskResponse::new)
                .collect(Collectors.toList());
    }

    // ── Auto-generation engine ────────────────────────────────────────

    private void autoGenerateWeeks(MonthlyPlan plan, User user, Discipline discipline) {
        String[] themes = {
                "Foundation",
                "Build Momentum",
                "Push Further",
                "Review & Lock In"
        };
        String[] focuses = {
                "Establish the core habit. Focus on starting — not perfecting.",
                "Increase frequency. Build on what worked in Week 1.",
                "Push beyond comfort. Challenge yourself with harder tasks.",
                "Review progress. Lock in what worked. Drop what did not."
        };

        LocalDate firstDay  = LocalDate.of(plan.getYear(), plan.getMonth(), 1);
        LocalDate lastDay   = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        for (int w = 0; w < 4; w++) {
            LocalDate weekStart = firstDay.plusWeeks(w);
            // Clamp week end to the last day of the month
            LocalDate weekEnd   = weekStart.plusDays(6);
            if (weekEnd.isAfter(lastDay)) weekEnd = lastDay;

            // Skip if week starts beyond the month
            if (weekStart.isAfter(lastDay)) break;

            WeeklyBreakdown week = WeeklyBreakdown.builder()
                    .monthlyPlan(plan)
                    .user(user)
                    .weekNumber(w + 1)
                    .theme(themes[w])
                    .focus(focuses[w])
                    .startDate(weekStart)
                    .endDate(weekEnd)
                    .build();

            autoGenerateTasks(weeklyBreakdownRepository.save(week), user, discipline, w + 1);
        }
    }

    private void autoGenerateTasks(WeeklyBreakdown week, User user,
                                   Discipline discipline, int weekNum) {
        String disciplineAction = discipline != null ? discipline.getDailyAction() : null;
        String areaName         = discipline != null ? discipline.getAreaName() : "Personal Growth";

        // ✅ FIXED: was TaskCategory.DISCIPLINE which does not exist in the enum.
        // Tasks linked to a discipline use STUDY; personal growth tasks use PERSONAL.
        DailyTask.TaskCategory category = discipline != null
                ? DailyTask.TaskCategory.STUDY
                : DailyTask.TaskCategory.PERSONAL;

        for (int day = 0; day < 5; day++) {
            LocalDate taskDate = week.getStartDate().plusDays(day);

            // Skip dates in the past
            if (taskDate.isBefore(LocalDate.now())) continue;

            // Skip dates beyond the week's end (month boundary)
            if (taskDate.isAfter(week.getEndDate())) break;

            DailyTask task = DailyTask.builder()
                    .weeklyBreakdown(week)
                    .user(user)
                    .title(buildTaskTitle(weekNum, day, areaName, disciplineAction))
                    .description(buildTaskDescription(weekNum))
                    .category(category)
                    // First two days of week are HIGH priority, rest MEDIUM
                    .priority(day < 2
                            ? DailyTask.TaskPriority.HIGH
                            : DailyTask.TaskPriority.MEDIUM)
                    .dueDate(taskDate)
                    .dueTime(java.time.LocalTime.of(9, 0))
                    .durationMinutes(30)
                    .build();

            dailyTaskRepository.save(task);
        }
    }

    private String buildTaskTitle(int week, int day, String area, String action) {
        if (action != null && !action.isBlank()) {
            return switch (day) {
                case 0 -> "Start Week " + week + ": " + action;
                case 1 -> "Day 2 — " + action;
                case 2 -> "Midweek check-in: " + area;
                case 3 -> "Day 4 — " + action;
                case 4 -> "Week " + week + " review: " + area;
                default -> action;
            };
        }
        return switch (day) {
            case 0 -> "Week " + week + " — Set your intention";
            case 1 -> "Execute day 2 plan";
            case 2 -> "Midweek review";
            case 3 -> "Push through day 4";
            case 4 -> "Week " + week + " wrap-up";
            default -> "Daily task";
        };
    }

    private String buildTaskDescription(int week) {
        return switch (week) {
            case 1 -> "Week 1 — Foundation. Focus on starting without judgment.";
            case 2 -> "Week 2 — Build Momentum. Increase consistency from last week.";
            case 3 -> "Week 3 — Push Further. Go beyond your comfort zone today.";
            case 4 -> "Week 4 — Lock In. Review, reflect, and solidify the habit.";
            default -> "Stay consistent. Small steps compound.";
        };
    }

    // ── Private helpers ───────────────────────────────────────────────

    private PlanDto.MonthlyPlanResponse buildPlanResponse(MonthlyPlan plan) {
        PlanDto.MonthlyPlanResponse response = new PlanDto.MonthlyPlanResponse(plan);
        List<PlanDto.WeeklyBreakdownResponse> weeks = weeklyBreakdownRepository
                .findByMonthlyPlanIdOrderByWeekNumber(plan.getId())
                .stream()
                .map(this::buildWeekResponse)
                .collect(Collectors.toList());
        response.setWeeks(weeks);
        return response;
    }

    private PlanDto.WeeklyBreakdownResponse buildWeekResponse(WeeklyBreakdown w) {
        PlanDto.WeeklyBreakdownResponse response = new PlanDto.WeeklyBreakdownResponse(w);

        List<PlanDto.DailyTaskResponse> tasks = dailyTaskRepository
                .findByWeeklyBreakdownIdOrderByDueDateAscPriorityAsc(w.getId())
                .stream()
                .map(PlanDto.DailyTaskResponse::new)
                .collect(Collectors.toList());

        response.setTasks(tasks);
        response.setTotalTasks(tasks.size());
        response.setCompletedTasks((int) tasks.stream()
                .filter(t -> t.getStatus() == DailyTask.TaskStatus.DONE)
                .count());
        return response;
    }

    private MonthlyPlan getOwnedPlan(Long userId, Long planId) {
        MonthlyPlan plan = monthlyPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        if (!plan.getUser().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        return plan;
    }

    private DailyTask getOwnedTask(Long userId, Long taskId) {
        DailyTask task = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getUser().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        return task;
    }
}
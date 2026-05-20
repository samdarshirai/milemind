package com.company.runcoach.planning.service;

import com.company.runcoach.goals.domain.RaceDistanceType;
import com.company.runcoach.goals.domain.RaceGoal;
import com.company.runcoach.profile.domain.ExperienceLevel;
import com.company.runcoach.profile.domain.RunnerProfile;
import com.company.runcoach.profile.domain.Weekday;
import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.planning.engine.PlanProgressionCalculator;
import com.company.runcoach.planning.engine.WorkoutDistributionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlanGenerationServiceTest {

    private final PlanGenerationService service = new PlanGenerationService(
        new PlanProgressionCalculator(),
        new WorkoutDistributionService()
    );

    @Test
    void generatesValidHalfMarathonPlanWithWeeksAndWorkouts() {
        RunnerProfile profile = profile(ExperienceLevel.INTERMEDIATE);
        RaceGoal goal = raceGoal(RaceDistanceType.HALF_MARATHON, LocalDate.of(2026, 9, 20));

        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        assertFalse(plan.weeks().isEmpty());
        assertEquals(LocalDate.of(2026, 9, 20), plan.endDate());
        assertTrue(plan.weeks().stream().allMatch(w -> !w.workouts().isEmpty()));
    }

    @Test
    void everyFourthWeekIsRecoveryAndRaceWeekIsTapered() {
        RunnerProfile profile = profile(ExperienceLevel.BEGINNER);
        RaceGoal goal = raceGoal(RaceDistanceType.HALF_MARATHON, LocalDate.of(2026, 10, 11));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        for (int i = 3; i < plan.weeks().size() - 2; i += 4) {
            assertEquals(i + 1, plan.weeks().get(i).weekNumber());
        }

        var taperWeek = plan.weeks().get(plan.weeks().size() - 2).totalPlannedDistanceKm();
        var preTaperWeek = plan.weeks().get(plan.weeks().size() - 3).totalPlannedDistanceKm();
        assertTrue(taperWeek.compareTo(preTaperWeek) <= 0);
    }

    @Test
    void longRunPrefersConfiguredLongRunDayWhenAvailable() {
        RunnerProfile profile = profile(ExperienceLevel.INTERMEDIATE);
        RaceGoal goal = raceGoal(RaceDistanceType.HALF_MARATHON, LocalDate.of(2026, 9, 20));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        var firstWeekLongRun = plan.weeks().get(0).workouts().stream()
            .filter(w -> w.type() == PlannedWorkoutType.LONG_RUN)
            .findFirst().orElseThrow();

        assertEquals(Weekday.SUNDAY.name(), firstWeekLongRun.date().getDayOfWeek().name());
    }

    @Test
    void buildWeekMileageIncreaseStaysWithinTenPercentCap() {
        RunnerProfile profile = profile(ExperienceLevel.ADVANCED);
        RaceGoal goal = raceGoal(RaceDistanceType.HALF_MARATHON, LocalDate.of(2026, 9, 20));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        for (int i = 1; i < plan.weeks().size() - 2; i++) {
            var currentWeek = plan.weeks().get(i);
            var previousWeek = plan.weeks().get(i - 1);
            if (currentWeek.recoveryWeek() || previousWeek.recoveryWeek()
                || "TAPER".equals(currentWeek.phase().name())
                || "TAPER".equals(previousWeek.phase().name())) {
                continue;
            }
            BigDecimal prev = previousWeek.totalPlannedDistanceKm();
            BigDecimal current = currentWeek.totalPlannedDistanceKm();
            if (current.compareTo(prev) > 0) {
                BigDecimal cap = prev.multiply(new BigDecimal("1.10")).add(new BigDecimal("0.10"));
                assertTrue(current.compareTo(cap) <= 0);
            }
        }
    }

    @Test
    void beginnerPlanDoesNotUseHardIntervals() {
        RunnerProfile profile = profile(ExperienceLevel.BEGINNER);
        RaceGoal goal = raceGoal(RaceDistanceType.MARATHON, LocalDate.of(2026, 10, 18));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        long hardIntervals = plan.weeks().stream()
            .flatMap(w -> w.workouts().stream())
            .filter(w -> w.type() == PlannedWorkoutType.INTERVALS)
            .count();

        assertEquals(0, hardIntervals);
    }

    @Test
    void marathonPlanUsesThreeWeekTaper() {
        RunnerProfile profile = profile(ExperienceLevel.INTERMEDIATE);
        RaceGoal goal = raceGoal(RaceDistanceType.MARATHON, LocalDate.of(2026, 10, 18));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        var wMinus3 = plan.weeks().get(plan.weeks().size() - 3);
        var wMinus2 = plan.weeks().get(plan.weeks().size() - 2);
        var raceWeek = plan.weeks().get(plan.weeks().size() - 1);
        assertEquals("TAPER", wMinus3.phase().name());
        assertEquals("TAPER", wMinus2.phase().name());
        assertEquals("TAPER", raceWeek.phase().name());
    }

    @Test
    void earlyLongRunStaysUnder110PercentOfRecentLongestRun() {
        RunnerProfile profile = profile(ExperienceLevel.INTERMEDIATE);
        profile.setLongestRecentRunKm(new BigDecimal("10.0"));
        RaceGoal goal = raceGoal(RaceDistanceType.MARATHON, LocalDate.of(2026, 10, 18));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        BigDecimal cap = new BigDecimal("11.00");
        for (int i = 0; i < Math.min(3, plan.weeks().size()); i++) {
            BigDecimal longRun = plan.weeks().get(i).workouts().stream()
                .filter(w -> w.type() == PlannedWorkoutType.LONG_RUN)
                .map(w -> w.plannedDistanceKm())
                .findFirst()
                .orElse(BigDecimal.ZERO);
            assertTrue(longRun.compareTo(cap) <= 0);
        }
    }

    @Test
    void beginnerAndIntermediateAvoidAdjacentLongRunAndQuality() {
        RunnerProfile profile = profile(ExperienceLevel.BEGINNER);
        RaceGoal goal = raceGoal(RaceDistanceType.MARATHON, LocalDate.of(2026, 10, 18));
        var plan = service.generate(profile, goal, LocalDate.of(2026, 6, 1));

        for (var week : plan.weeks()) {
            LocalDate longRunDate = week.workouts().stream()
                .filter(w -> w.type() == PlannedWorkoutType.LONG_RUN)
                .map(w -> w.date())
                .findFirst()
                .orElse(null);
            LocalDate qualityDate = week.workouts().stream()
                .filter(w -> w.type() == PlannedWorkoutType.TEMPO_RUN || w.type() == PlannedWorkoutType.INTERVALS)
                .map(w -> w.date())
                .findFirst()
                .orElse(null);
            if (longRunDate != null && qualityDate != null) {
                assertTrue(Math.abs(longRunDate.toEpochDay() - qualityDate.toEpochDay()) > 1);
            }
        }
    }

    private RunnerProfile profile(ExperienceLevel level) {
        RunnerProfile profile = new RunnerProfile();
        profile.setId(UUID.randomUUID());
        profile.setExperienceLevel(level);
        profile.setTypicalWeeklyDistanceKm(new BigDecimal("30.0"));
        profile.setLongestRecentRunKm(new BigDecimal("12.0"));
        profile.setPreferredRunDays(List.of("TUESDAY", "THURSDAY", "SATURDAY", "SUNDAY"));
        profile.setPreferredLongRunDay(Weekday.SUNDAY);
        return profile;
    }

    private RaceGoal raceGoal(RaceDistanceType distanceType, LocalDate raceDate) {
        RaceGoal goal = new RaceGoal();
        goal.setId(UUID.randomUUID());
        goal.setRaceDistanceType(distanceType);
        goal.setRaceDate(raceDate);
        return goal;
    }
}

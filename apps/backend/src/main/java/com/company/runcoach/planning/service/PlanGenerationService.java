package com.company.runcoach.planning.service;

import com.company.runcoach.goals.domain.RaceDistanceType;
import com.company.runcoach.goals.domain.RaceGoal;
import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.planning.domain.TrainingPhase;
import com.company.runcoach.planning.domain.WorkoutIntensity;
import com.company.runcoach.planning.engine.GeneratedPlanDraft;
import com.company.runcoach.planning.engine.PlanProgressionCalculator;
import com.company.runcoach.planning.engine.WorkoutDistributionService;
import com.company.runcoach.profile.domain.ExperienceLevel;
import com.company.runcoach.profile.domain.RunnerProfile;
import com.company.runcoach.profile.domain.Weekday;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PlanGenerationService {

    private final PlanProgressionCalculator progressionCalculator;
    private final WorkoutDistributionService workoutDistributionService;

    public PlanGenerationService(
        PlanProgressionCalculator progressionCalculator,
        WorkoutDistributionService workoutDistributionService
    ) {
        this.progressionCalculator = progressionCalculator;
        this.workoutDistributionService = workoutDistributionService;
    }

    public GeneratedPlanDraft generate(RunnerProfile profile, RaceGoal raceGoal, LocalDate startDate) {
        LocalDate raceDate = raceGoal.getRaceDate();
        int weeks = (int) Math.ceil((raceDate.toEpochDay() - startDate.toEpochDay() + 1) / 7.0);

        Set<DayOfWeek> availableDays = parseAvailableDays(profile);
        List<GeneratedPlanDraft.GeneratedWeekDraft> weekDrafts = new ArrayList<>();

        BigDecimal baseline = profile.getTypicalWeeklyDistanceKm().max(BigDecimal.valueOf(8));
        BigDecimal previousVolume = baseline;

        for (int i = 1; i <= weeks; i++) {
            LocalDate weekStart = startDate.plusDays((long) (i - 1) * 7);
            LocalDate weekEnd = minDate(weekStart.plusDays(6), raceDate);
            int weeksRemaining = weeks - i + 1;

            boolean recoveryWeek = i % 4 == 0 && weeksRemaining > 3;
            BigDecimal weekVolume = calculateWeekVolume(previousVolume, profile.getExperienceLevel(), i, weeksRemaining);
            List<LocalDate> workoutDates = availableDatesInRange(weekStart, weekEnd, availableDays);
            if (workoutDates.isEmpty()) {
                weekDrafts.add(new GeneratedPlanDraft.GeneratedWeekDraft(
                    i,
                    phaseFor(i, weeks, raceGoal.getRaceDistanceType()),
                    recoveryWeek,
                    weekStart,
                    weekEnd,
                    BigDecimal.ZERO,
                    List.of()
                ));
                previousVolume = weekVolume;
                continue;
            }

            LocalDate preferredLongRunDate = preferredDateInWeek(weekStart, profile.getPreferredLongRunDay());
            LocalDate longRunDate = workoutDistributionService.chooseLongRunDate(workoutDates, preferredLongRunDate);

            List<PlannedWorkoutType> types = workoutDistributionService.weekWorkoutTypes(
                workoutDates.size(),
                profile.getExperienceLevel(),
                i
            );

            List<GeneratedPlanDraft.GeneratedWorkoutDraft> workouts = buildWorkouts(
                workoutDates,
                longRunDate,
                weekVolume,
                types,
                profile.getExperienceLevel(),
                weeksRemaining,
                raceGoal.getRaceDistanceType(),
                raceDate,
                i,
                profile.getLongestRecentRunKm()
            );

            BigDecimal total = workouts.stream()
                .map(w -> w.plannedDistanceKm() == null ? BigDecimal.ZERO : w.plannedDistanceKm())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

            weekDrafts.add(new GeneratedPlanDraft.GeneratedWeekDraft(
                i,
                phaseFor(i, weeks, raceGoal.getRaceDistanceType()),
                recoveryWeek,
                weekStart,
                weekEnd,
                total,
                workouts
            ));
            previousVolume = total.max(BigDecimal.valueOf(1));
        }

        return new GeneratedPlanDraft(startDate, raceDate, weekDrafts);
    }

    private BigDecimal calculateWeekVolume(BigDecimal previous, ExperienceLevel level, int weekNumber, int weeksRemaining) {
        BigDecimal candidate;
        if (weeksRemaining == 1) {
            candidate = progressionCalculator.raceWeekVolume(previous);
            return candidate;
        }
        if (weeksRemaining == 2) {
            candidate = progressionCalculator.taperWeekMinusTwo(previous);
            return candidate;
        }
        if (weeksRemaining == 3 && level != ExperienceLevel.BEGINNER) {
            candidate = previous.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);
            return candidate;
        }
        if (weekNumber % 4 == 0) {
            candidate = progressionCalculator.recoveryWeekVolume(previous);
            return candidate;
        }
        candidate = progressionCalculator.nextWeekVolume(previous, level);
        BigDecimal cap = previous.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
        return candidate.min(cap);
    }

    private List<GeneratedPlanDraft.GeneratedWorkoutDraft> buildWorkouts(
        List<LocalDate> dates,
        LocalDate longRunDate,
        BigDecimal weekVolume,
        List<PlannedWorkoutType> types,
        ExperienceLevel level,
        int weeksRemaining,
        RaceDistanceType raceDistanceType,
        LocalDate raceDate,
        int weekNumber,
        BigDecimal longestRecentRunKm
    ) {
        List<GeneratedPlanDraft.GeneratedWorkoutDraft> workouts = new ArrayList<>();

        BigDecimal longRunShare = raceDistanceType == RaceDistanceType.MARATHON
            ? new BigDecimal("0.35")
            : new BigDecimal("0.30");
        BigDecimal longRunDistance = weekVolume.multiply(longRunShare).setScale(2, RoundingMode.HALF_UP);
        if (weekNumber <= 3) {
            BigDecimal capped = profileLongestRunCap(raceDistanceType, weekVolume, longestRecentRunKm);
            if (longRunDistance.compareTo(capped) > 0) {
                longRunDistance = capped;
            }
        }

        BigDecimal nonLongTotal = weekVolume.subtract(longRunDistance).max(BigDecimal.ZERO);
        int nonLongCount = Math.max(1, dates.size() - 1);
        BigDecimal eachNonLong = nonLongTotal.divide(BigDecimal.valueOf(nonLongCount), 2, RoundingMode.HALF_UP);

        int typeIndex = 0;
        LocalDate qualityDate = null;
        for (LocalDate date : dates.stream().sorted(Comparator.naturalOrder()).toList()) {
            PlannedWorkoutType type = date.equals(longRunDate)
                ? PlannedWorkoutType.LONG_RUN
                : types.get(Math.min(typeIndex + 1, types.size() - 1));
            if (!date.equals(longRunDate)) {
                typeIndex++;
            }

            if (isQuality(type)) {
                qualityDate = date;
            }
            if ((level == ExperienceLevel.BEGINNER || level == ExperienceLevel.INTERMEDIATE)
                && qualityDate != null
                && !date.equals(qualityDate)
                && type == PlannedWorkoutType.LONG_RUN
                && Math.abs(date.toEpochDay() - qualityDate.toEpochDay()) == 1) {
                type = PlannedWorkoutType.EASY_RUN;
            }

            BigDecimal distance = type == PlannedWorkoutType.LONG_RUN ? longRunDistance : eachNonLong;
            if (weeksRemaining == 1 && date.equals(raceDate)) {
                type = PlannedWorkoutType.LONG_RUN;
                distance = raceDistanceType == RaceDistanceType.MARATHON ? new BigDecimal("42.20") : new BigDecimal("21.10");
            }

            Map<String, Object> rationale = new HashMap<>();
            rationale.put("whyThisWorkout", description(type, weeksRemaining == 1 && date.equals(raceDate)));
            rationale.put("changeReasonCodes", List.of());

            workouts.add(new GeneratedPlanDraft.GeneratedWorkoutDraft(
                date,
                type,
                subtype(type),
                distance,
                null,
                intensity(type, level),
                defaultStructure(type),
                rationale
            ));
        }
        return workouts;
    }

    private String subtype(PlannedWorkoutType type) {
        return switch (type) {
            case EASY_RUN -> "AEROBIC_EASY";
            case LONG_RUN -> "LONG_AEROBIC";
            case INTERVALS -> "CRUISE_INTERVALS";
            case TEMPO_RUN -> "THRESHOLD_LITE";
            case REST -> "REST";
            case RECOVERY_RUN -> "RECOVERY_AEROBIC";
        };
    }

    private WorkoutIntensity intensity(PlannedWorkoutType type, ExperienceLevel level) {
        return switch (type) {
            case EASY_RUN, LONG_RUN, RECOVERY_RUN -> WorkoutIntensity.EASY;
            case TEMPO_RUN -> WorkoutIntensity.MODERATE;
            case INTERVALS -> level == ExperienceLevel.BEGINNER ? WorkoutIntensity.MODERATE : WorkoutIntensity.HARD;
            case REST -> WorkoutIntensity.REST;
        };
    }

    private String description(PlannedWorkoutType type, boolean raceDay) {
        if (raceDay) {
            return "Race day effort. Start controlled and finish strong.";
        }
        return switch (type) {
            case EASY_RUN, RECOVERY_RUN -> "Comfortable conversational pace.";
            case LONG_RUN -> "Steady long run at an easy effort.";
            case TEMPO_RUN -> "Controlled moderate effort. Do not sprint.";
            case INTERVALS -> "Short faster efforts with easy recovery between repetitions.";
            case REST -> "Rest day for recovery.";
        };
    }

    private Map<String, Object> defaultStructure(PlannedWorkoutType type) {
        Map<String, Object> structure = new HashMap<>();
        structure.put("segments", List.of(Map.of("segmentType", "MAIN_SET", "cue", type.name())));
        return structure;
    }

    private boolean isQuality(PlannedWorkoutType type) {
        return type == PlannedWorkoutType.TEMPO_RUN || type == PlannedWorkoutType.INTERVALS;
    }

    private BigDecimal profileLongestRunCap(
        RaceDistanceType raceDistanceType,
        BigDecimal weekVolume,
        BigDecimal longestRecentRunKm
    ) {
        BigDecimal percentCap = raceDistanceType == RaceDistanceType.MARATHON
            ? weekVolume.multiply(new BigDecimal("0.40"))
            : weekVolume.multiply(new BigDecimal("0.35"));
        BigDecimal longRunHistoryCap = longestRecentRunKm.multiply(new BigDecimal("1.10"));
        return percentCap.min(longRunHistoryCap).setScale(2, RoundingMode.HALF_UP);
    }

    private TrainingPhase phaseFor(int weekNumber, int totalWeeks, RaceDistanceType raceDistanceType) {
        int taperWeeks = raceDistanceType == RaceDistanceType.MARATHON ? 3 : 2;
        if (totalWeeks - weekNumber < taperWeeks) {
            return TrainingPhase.TAPER;
        }
        int cutoff = Math.max(2, totalWeeks / 3);
        if (weekNumber <= cutoff) {
            return TrainingPhase.BASE;
        }
        if (weekNumber <= cutoff * 2) {
            return TrainingPhase.BUILD;
        }
        return TrainingPhase.RACE_SPECIFIC;
    }

    private Set<DayOfWeek> parseAvailableDays(RunnerProfile profile) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (String day : profile.getPreferredRunDays()) {
            days.add(DayOfWeek.valueOf(day));
        }
        return days;
    }

    private LocalDate preferredDateInWeek(LocalDate weekStart, Weekday preferred) {
        DayOfWeek preferredDay = DayOfWeek.valueOf(preferred.name());
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            if (date.getDayOfWeek() == preferredDay) {
                return date;
            }
        }
        return weekStart;
    }

    private List<LocalDate> availableDatesInRange(LocalDate start, LocalDate end, Set<DayOfWeek> days) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (days.contains(cursor.getDayOfWeek())) {
                dates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    private LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}

package com.company.runcoach.planning.engine;

import com.company.runcoach.planning.domain.PlannedWorkoutType;
import com.company.runcoach.profile.domain.ExperienceLevel;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkoutDistributionService {

    public List<PlannedWorkoutType> weekWorkoutTypes(int runDays, ExperienceLevel level, int weekNumber) {
        int safeRunDays = Math.max(1, runDays);
        List<PlannedWorkoutType> types = new ArrayList<>();
        if (safeRunDays == 1) {
            types.add(PlannedWorkoutType.LONG_RUN);
            return types;
        }

        types.add(PlannedWorkoutType.LONG_RUN);
        types.add(selectQualityType(level, weekNumber));

        while (types.size() < safeRunDays) {
            if (types.size() == safeRunDays - 1 && safeRunDays >= 4) {
                types.add(PlannedWorkoutType.RECOVERY_RUN);
            } else {
                types.add(PlannedWorkoutType.EASY_RUN);
            }
        }
        return types;
    }

    public LocalDate chooseLongRunDate(List<LocalDate> availableDates, LocalDate preferredDateInWeek) {
        if (availableDates.contains(preferredDateInWeek)) {
            return preferredDateInWeek;
        }
        return availableDates.get(availableDates.size() - 1);
    }

    private PlannedWorkoutType selectQualityType(ExperienceLevel level, int weekNumber) {
        if (level == ExperienceLevel.BEGINNER) {
            return weekNumber % 2 == 0 ? PlannedWorkoutType.TEMPO_RUN : PlannedWorkoutType.EASY_RUN;
        }
        return weekNumber % 2 == 0 ? PlannedWorkoutType.INTERVALS : PlannedWorkoutType.TEMPO_RUN;
    }
}

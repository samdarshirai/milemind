package com.company.runcoach.planning.api;

import com.company.runcoach.planning.service.TrainingPlanService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/planned-workouts")
public class PlannedWorkoutController {

    private final TrainingPlanService trainingPlanService;

    public PlannedWorkoutController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @GetMapping("/{plannedWorkoutId}")
    public PlannedWorkoutDetailResponse getPlannedWorkout(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID plannedWorkoutId
    ) {
        return trainingPlanService.getPlannedWorkout(user.userId(), plannedWorkoutId);
    }
}

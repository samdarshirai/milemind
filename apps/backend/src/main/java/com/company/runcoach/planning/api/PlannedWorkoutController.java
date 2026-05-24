package com.company.runcoach.planning.api;

import com.company.runcoach.adaptation.service.AdaptationDecisionService;
import com.company.runcoach.planning.service.TrainingPlanService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/planned-workouts")
public class PlannedWorkoutController {

    private final TrainingPlanService trainingPlanService;
    private final AdaptationDecisionService adaptationDecisionService;

    public PlannedWorkoutController(
        TrainingPlanService trainingPlanService,
        AdaptationDecisionService adaptationDecisionService
    ) {
        this.trainingPlanService = trainingPlanService;
        this.adaptationDecisionService = adaptationDecisionService;
    }

    @GetMapping("/{plannedWorkoutId}")
    public PlannedWorkoutDetailResponse getPlannedWorkout(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID plannedWorkoutId
    ) {
        return trainingPlanService.getPlannedWorkout(user.userId(), plannedWorkoutId);
    }

    @PostMapping("/{plannedWorkoutId}/skip")
    public PlannedWorkoutMutationResponse skipPlannedWorkout(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID plannedWorkoutId,
        @Valid @RequestBody SkipPlannedWorkoutRequest request
    ) {
        return adaptationDecisionService.skipWorkout(
            user.userId(),
            plannedWorkoutId,
            request.reason(),
            request.expectedPlanVersion()
        );
    }

    @PostMapping("/{plannedWorkoutId}/reschedule")
    public PlannedWorkoutMutationResponse reschedulePlannedWorkout(
        @AuthenticationPrincipal AuthenticatedUser user,
        @PathVariable UUID plannedWorkoutId,
        @Valid @RequestBody ReschedulePlannedWorkoutRequest request
    ) {
        return adaptationDecisionService.rescheduleWorkout(
            user.userId(),
            plannedWorkoutId,
            request.targetDate(),
            request.expectedPlanVersion()
        );
    }
}

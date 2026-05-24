package com.company.runcoach.planning.service;

import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.service.AdaptationDecisionService;
import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.planning.api.CreateWorkoutCompletionRequest;
import com.company.runcoach.planning.api.CreateWorkoutCompletionResponse;
import com.company.runcoach.planning.api.PlannedWorkoutMutationResponse;
import com.company.runcoach.planning.domain.PlannedWorkoutStatus;
import com.company.runcoach.planning.domain.WorkoutCompletion;
import com.company.runcoach.planning.domain.WorkoutCompletionSource;
import com.company.runcoach.planning.repo.PlannedWorkoutRepository;
import com.company.runcoach.planning.repo.WorkoutCompletionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WorkoutCompletionService {

    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final WorkoutCompletionRepository workoutCompletionRepository;
    private final AdaptationDecisionService adaptationDecisionService;

    public WorkoutCompletionService(
        PlannedWorkoutRepository plannedWorkoutRepository,
        WorkoutCompletionRepository workoutCompletionRepository,
        AdaptationDecisionService adaptationDecisionService
    ) {
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.workoutCompletionRepository = workoutCompletionRepository;
        this.adaptationDecisionService = adaptationDecisionService;
    }

    @Transactional
    public CreateWorkoutCompletionResponse completeWorkout(UUID userId, CreateWorkoutCompletionRequest request) {
        var plannedWorkout = plannedWorkoutRepository.findByIdAndUser_Id(request.plannedWorkoutId(), userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Planned workout not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("plannedWorkoutId", "not_found"))));

        if (plannedWorkout.getStatus() != PlannedWorkoutStatus.PLANNED) {
            throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("plannedWorkoutId", "complete_not_allowed_for_status")));
        }

        OffsetDateTime now = OffsetDateTime.now();
        WorkoutCompletion completion = new WorkoutCompletion();
        completion.setId(UUID.randomUUID());
        completion.setPlannedWorkout(plannedWorkout);
        completion.setUser(plannedWorkout.getUser());
        completion.setCompletionSource(WorkoutCompletionSource.MANUAL);
        completion.setCompletedAt(now);
        completion.setActualDistanceKm(request.actualDistanceKm());
        completion.setActualDurationMin(request.actualDurationMin());
        completion.setCreatedAt(now);
        WorkoutCompletion savedCompletion = workoutCompletionRepository.save(completion);

        AdaptationDecision decision = adaptationDecisionService.adaptFromWorkoutCompletion(
            userId,
            request.plannedWorkoutId(),
            request.actualDistanceKm(),
            request.actualDurationMin()
        );

        int planVersion = plannedWorkoutRepository.findById(request.plannedWorkoutId())
            .map(workout -> workout.getTrainingPlan().getPlanVersion())
            .orElse(plannedWorkout.getTrainingPlan().getPlanVersion());

        PlannedWorkoutMutationResponse.AdaptationSummary adaptation = decision == null
            ? null
            : new PlannedWorkoutMutationResponse.AdaptationSummary(
                decision.getId(),
                decision.getDecisionSummary(),
                decision.getAffectedFromDate(),
                decision.getAffectedToDate(),
                decision.getChangedWorkoutIds().stream().map(UUID::fromString).toList()
            );

        return new CreateWorkoutCompletionResponse(
            savedCompletion.getId(),
            request.plannedWorkoutId(),
            planVersion,
            decision != null,
            adaptation
        );
    }
}

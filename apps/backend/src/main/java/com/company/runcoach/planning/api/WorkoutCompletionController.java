package com.company.runcoach.planning.api;

import com.company.runcoach.planning.service.WorkoutCompletionService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/workout-completions")
public class WorkoutCompletionController {

    private final WorkoutCompletionService workoutCompletionService;

    public WorkoutCompletionController(WorkoutCompletionService workoutCompletionService) {
        this.workoutCompletionService = workoutCompletionService;
    }

    @PostMapping
    public CreateWorkoutCompletionResponse createCompletion(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody CreateWorkoutCompletionRequest request
    ) {
        return workoutCompletionService.completeWorkout(user.userId(), request);
    }
}

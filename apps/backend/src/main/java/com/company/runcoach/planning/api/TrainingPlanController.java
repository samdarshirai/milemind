package com.company.runcoach.planning.api;

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
@RequestMapping("/v1/plans")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @PostMapping("/generate")
    public GeneratePlanResponse generatePlan(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody GeneratePlanRequest request
    ) {
        return trainingPlanService.generatePlan(user.userId(), request);
    }

    @GetMapping("/{planId}")
    public PlanByIdResponse getPlan(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID planId) {
        return trainingPlanService.getPlanById(user.userId(), planId);
    }

    @GetMapping("/current")
    public CurrentTrainingPlanResponse getCurrentPlan(@AuthenticationPrincipal AuthenticatedUser user) {
        return trainingPlanService.getCurrentPlan(user.userId());
    }

}

package com.company.runcoach.goals.api;

import com.company.runcoach.goals.service.RaceGoalService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/race-goals")
public class RaceGoalController {

    private final RaceGoalService raceGoalService;

    public RaceGoalController(RaceGoalService raceGoalService) {
        this.raceGoalService = raceGoalService;
    }

    @PostMapping
    public CreateRaceGoalResponse createRaceGoal(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody CreateRaceGoalRequest request
    ) {
        return raceGoalService.createRaceGoal(user.userId(), request);
    }

    @GetMapping("/current")
    public CurrentRaceGoalResponse currentRaceGoal(@AuthenticationPrincipal AuthenticatedUser user) {
        return raceGoalService.getCurrentRaceGoal(user.userId());
    }
}

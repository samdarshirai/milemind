package com.company.runcoach.adaptation.api;

import com.company.runcoach.adaptation.service.FatigueSignalService;
import com.company.runcoach.adaptation.service.InjuryFeedbackService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class CheckInController {

    private final FatigueSignalService fatigueSignalService;
    private final InjuryFeedbackService injuryFeedbackService;

    public CheckInController(FatigueSignalService fatigueSignalService, InjuryFeedbackService injuryFeedbackService) {
        this.fatigueSignalService = fatigueSignalService;
        this.injuryFeedbackService = injuryFeedbackService;
    }

    @PostMapping("/fatigue-signals")
    public CreateFatigueSignalResponse createFatigueSignal(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody CreateFatigueSignalRequest request
    ) {
        return fatigueSignalService.createOrUpdate(user.userId(), request);
    }

    @PostMapping("/injury-feedback")
    public CreateInjuryFeedbackResponse createInjuryFeedback(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody CreateInjuryFeedbackRequest request
    ) {
        return injuryFeedbackService.create(user.userId(), request);
    }
}

package com.company.runcoach.progress.api;

import com.company.runcoach.platform.security.AuthenticatedUser;
import com.company.runcoach.progress.service.ProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/summary")
    public ProgressSummaryResponse summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return progressService.getSummary(user.userId());
    }
}

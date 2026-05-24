package com.company.runcoach.insights.api;

import com.company.runcoach.insights.service.TodayInsightService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/insights")
public class TodayInsightController {

    private final TodayInsightService todayInsightService;

    public TodayInsightController(TodayInsightService todayInsightService) {
        this.todayInsightService = todayInsightService;
    }

    @GetMapping("/today")
    public TodayInsightResponse today(@AuthenticationPrincipal AuthenticatedUser user) {
        return todayInsightService.getToday(user.userId());
    }
}

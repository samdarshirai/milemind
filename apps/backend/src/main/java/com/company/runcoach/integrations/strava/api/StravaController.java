package com.company.runcoach.integrations.strava.api;

import com.company.runcoach.integrations.strava.service.StravaConnectionService;
import com.company.runcoach.platform.security.AuthenticatedUser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/integrations/strava")
public class StravaController {

    private final StravaConnectionService stravaConnectionService;

    public StravaController(StravaConnectionService stravaConnectionService) {
        this.stravaConnectionService = stravaConnectionService;
    }

    @PostMapping("/connect-session")
    public StravaConnectSessionResponse connectSession(@AuthenticationPrincipal AuthenticatedUser user) {
        requireUser(user);
        return stravaConnectionService.createConnectSession(user.userId());
    }

    @GetMapping("/status")
    public StravaStatusResponse status(@AuthenticationPrincipal AuthenticatedUser user) {
        requireUser(user);
        return stravaConnectionService.status(user.userId());
    }

    @DeleteMapping("/connection")
    public StravaStatusResponse disconnect(@AuthenticationPrincipal AuthenticatedUser user) {
        requireUser(user);
        return stravaConnectionService.disconnect(user.userId());
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
        @RequestParam(name = "state", required = false) String state,
        @RequestParam(name = "code", required = false) String code,
        @RequestParam(name = "error", required = false) String error
    ) {
        String redirect = stravaConnectionService.handleCallback(state, code, error);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, redirect).build();
    }

    private void requireUser(AuthenticatedUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
    }
}

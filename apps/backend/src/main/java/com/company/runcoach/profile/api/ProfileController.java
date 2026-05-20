package com.company.runcoach.profile.api;

import com.company.runcoach.platform.security.AuthenticatedUser;
import com.company.runcoach.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/users/onboarding")
    public OnboardingProfileResponse onboarding(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody OnboardingProfileRequest request
    ) {
        return profileService.createOrUpdateOnboardingProfile(user.userId(), request.profile());
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
        return profileService.getProfile(user.userId());
    }

    @PutMapping("/profile")
    public ProfileResponse updateProfile(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return profileService.updateProfile(user.userId(), request);
    }
}

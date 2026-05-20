package com.company.runcoach.profile.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OnboardingProfileRequest(
    @NotNull @Valid ProfileInput profile
) {
}

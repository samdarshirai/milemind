package com.company.runcoach.profile.service;

import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.profile.api.OnboardingProfileResponse;
import com.company.runcoach.profile.api.ProfileInput;
import com.company.runcoach.profile.api.ProfileResponse;
import com.company.runcoach.profile.api.ProfileUpdateRequest;
import com.company.runcoach.profile.domain.ExperienceLevel;
import com.company.runcoach.profile.domain.GoalStyle;
import com.company.runcoach.profile.domain.RunnerProfile;
import com.company.runcoach.profile.domain.Sex;
import com.company.runcoach.profile.domain.Units;
import com.company.runcoach.profile.domain.Weekday;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ProfileService {

    private static final int MINIMUM_ADULT_AGE = 18;
    private static final int MINIMUM_PREFERRED_RUN_DAYS = 3;

    private final RunnerProfileRepository runnerProfileRepository;
    private final AppUserRepository appUserRepository;

    public ProfileService(RunnerProfileRepository runnerProfileRepository, AppUserRepository appUserRepository) {
        this.runnerProfileRepository = runnerProfileRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public OnboardingProfileResponse createOrUpdateOnboardingProfile(UUID userId, ProfileInput input) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));

        RunnerProfile profile = runnerProfileRepository.findByUser_Id(userId).orElseGet(() -> {
            RunnerProfile created = new RunnerProfile();
            created.setId(UUID.randomUUID());
            created.setUser(user);
            created.setCreatedAt(OffsetDateTime.now());
            return created;
        });

        applyOnboarding(profile, user, input);
        RunnerProfile saved = runnerProfileRepository.save(profile);
        return new OnboardingProfileResponse(userId, saved.getId());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        RunnerProfile profile = runnerProfileRepository.findByUser_Id(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Profile not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("profile", "not_found"))));

        AppUser user = profile.getUser();
        return toResponse(user, profile);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        RunnerProfile profile = runnerProfileRepository.findByUser_Id(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Profile not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("profile", "not_found"))));

        List<String> preferredRunDays = parseWeekdays(request.preferredRunDays(), "preferredRunDays");
        Weekday preferredLongRunDay = parseEnum(Weekday.class, request.preferredLongRunDay(), "preferredLongRunDay");
        validateLongRunDayInPreferredDays(preferredRunDays, preferredLongRunDay);
        profile.setPreferredRunDays(preferredRunDays);
        profile.setPreferredLongRunDay(preferredLongRunDay);
        validateStrengthDays(request.strengthDaysPerWeek());
        profile.setStrengthDaysPerWeek(request.strengthDaysPerWeek());
        profile.setUnits(parseEnum(Units.class, request.units(), "units"));

        if (request.injuryHistorySpecified()) {
            profile.setInjuryHistory(normalizeInjuryHistory(request.injuryHistory()));
        }

        AppUser user = profile.getUser();
        if (request.timezone() != null) {
            validateTimezone(request.timezone());
            user.setTimezone(request.timezone());
            user.setUpdatedAt(OffsetDateTime.now());
            appUserRepository.save(user);
        }

        profile.setUpdatedAt(OffsetDateTime.now());
        RunnerProfile saved = runnerProfileRepository.save(profile);
        return toResponse(user, saved);
    }

    private void applyOnboarding(RunnerProfile profile, AppUser user, ProfileInput input) {
        validateAdult(input.birthYear());
        validatePositive(input.typicalWeeklyDistanceKm(), "typicalWeeklyDistanceKm");
        validatePositive(input.longestRecentRunKm(), "longestRecentRunKm");
        validateStrengthDays(input.strengthDaysPerWeek());

        profile.setBirthYear(input.birthYear());
        profile.setSex(parseNullableEnum(Sex.class, input.sex(), "sex"));
        profile.setExperienceLevel(parseEnum(ExperienceLevel.class, input.experienceLevel(), "experienceLevel"));
        profile.setTypicalWeeklyDistanceKm(input.typicalWeeklyDistanceKm());
        profile.setLongestRecentRunKm(input.longestRecentRunKm());
        List<String> preferredRunDays = parseWeekdays(input.preferredRunDays(), "preferredRunDays");
        Weekday preferredLongRunDay = parseEnum(Weekday.class, input.preferredLongRunDay(), "preferredLongRunDay");
        validateLongRunDayInPreferredDays(preferredRunDays, preferredLongRunDay);
        profile.setPreferredRunDays(preferredRunDays);
        profile.setPreferredLongRunDay(preferredLongRunDay);
        profile.setGoalStyle(parseEnum(GoalStyle.class, input.goalStyle(), "goalStyle"));
        profile.setInjuryHistory(normalizeInjuryHistory(input.injuryHistory()));
        profile.setStrengthDaysPerWeek(input.strengthDaysPerWeek());
        profile.setUnits(parseEnum(Units.class, input.units(), "units"));

        if (input.timezone() != null) {
            validateTimezone(input.timezone());
            user.setTimezone(input.timezone());
            user.setUpdatedAt(OffsetDateTime.now());
            appUserRepository.save(user);
        }

        profile.setUpdatedAt(OffsetDateTime.now());
    }

    private void validateAdult(int birthYear) {
        int currentYear = Year.now(ZoneId.of("UTC")).getValue();
        int age = currentYear - birthYear;
        if (age < MINIMUM_ADULT_AGE) {
            throw validation("birthYear", "underage", "User must be at least 18 years old.");
        }
    }

    private void validatePositive(BigDecimal value, String field) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw validation(field, "must_be_positive", "Value must be positive.");
        }
    }

    private void validateStrengthDays(int strengthDaysPerWeek) {
        if (strengthDaysPerWeek < 0 || strengthDaysPerWeek > 2) {
            throw validation("strengthDaysPerWeek", "out_of_range", "Strength days must be between 0 and 2.");
        }
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
        } catch (Exception ex) {
            throw validation("timezone", "unsupported", "Unsupported timezone.");
        }
    }

    private List<String> parseWeekdays(List<String> days, String field) {
        if (days == null || days.isEmpty()) {
            throw validation(field, "empty", "Preferred run days cannot be empty.");
        }
        List<String> parsedDays = days.stream()
            .map(day -> parseEnum(Weekday.class, day, field).name())
            .distinct()
            .toList();
        if (parsedDays.size() < MINIMUM_PREFERRED_RUN_DAYS) {
            throw validation(field, "too_few", "Preferred run days must include at least 3 days.");
        }
        return parsedDays;
    }

    private void validateLongRunDayInPreferredDays(List<String> preferredRunDays, Weekday preferredLongRunDay) {
        if (!preferredRunDays.contains(preferredLongRunDay.name())) {
            throw validation(
                "preferredLongRunDay",
                "not_in_preferred_run_days",
                "Preferred long run day must be included in preferred run days."
            );
        }
    }

    private <E extends Enum<E>> E parseNullableEnum(Class<E> enumType, String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseEnum(enumType, value, field);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String field) {
        if (value == null || value.isBlank()) {
            throw validation(field, "invalid", "Invalid value.");
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw validation(field, "invalid", "Invalid value.");
        }
    }

    private ApiException validation(String field, String issue, String message) {
        return new ApiException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST,
            List.of(new ApiErrorDetail(field, issue)));
    }

    private Map<String, Object> normalizeInjuryHistory(Map<String, Object> injuryHistory) {
        if (injuryHistory == null) {
            return null;
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        Object hadRunningInjury = injuryHistory.get("hadRunningInjuryLast12Months");
        Object summary = injuryHistory.get("summary");

        for (String key : injuryHistory.keySet()) {
            if (!"hadRunningInjuryLast12Months".equals(key) && !"summary".equals(key)) {
                throw validation("injuryHistory", "invalid_key", "Injury history contains unsupported keys.");
            }
        }

        if (hadRunningInjury != null) {
            if (!(hadRunningInjury instanceof Boolean boolValue)) {
                throw validation(
                    "injuryHistory.hadRunningInjuryLast12Months",
                    "invalid_type",
                    "hadRunningInjuryLast12Months must be boolean."
                );
            }
            normalized.put("hadRunningInjuryLast12Months", boolValue);
        }

        if (summary != null) {
            if (!(summary instanceof String summaryValue)) {
                throw validation("injuryHistory.summary", "invalid_type", "summary must be a string.");
            }
            normalized.put("summary", summaryValue.trim());
        }

        return normalized.isEmpty() ? null : normalized;
    }

    private ProfileResponse toResponse(AppUser user, RunnerProfile profile) {
        return new ProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getTimezone(),
            new ProfileResponse.ProfileData(
                profile.getBirthYear(),
                profile.getSex() == null ? null : profile.getSex().name(),
                profile.getExperienceLevel().name(),
                profile.getTypicalWeeklyDistanceKm(),
                profile.getLongestRecentRunKm(),
                profile.getPreferredRunDays(),
                profile.getPreferredLongRunDay().name(),
                profile.getGoalStyle().name(),
                profile.getStrengthDaysPerWeek(),
                profile.getUnits().name(),
                profile.getInjuryHistory()
            )
        );
    }
}

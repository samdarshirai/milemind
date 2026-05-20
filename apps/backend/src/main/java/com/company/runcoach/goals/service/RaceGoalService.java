package com.company.runcoach.goals.service;

import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.goals.api.CreateRaceGoalRequest;
import com.company.runcoach.goals.api.CreateRaceGoalResponse;
import com.company.runcoach.goals.api.CurrentRaceGoalResponse;
import com.company.runcoach.goals.domain.GoalStyle;
import com.company.runcoach.goals.domain.RaceDistanceType;
import com.company.runcoach.goals.domain.RaceGoal;
import com.company.runcoach.goals.domain.RaceGoalStatus;
import com.company.runcoach.goals.repo.RaceGoalRepository;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.repo.AppUserRepository;
import com.company.runcoach.profile.repo.RunnerProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class RaceGoalService {

    private static final int HALF_MIN_WEEKS = 8;
    private static final int MARATHON_MIN_WEEKS = 12;

    private final RaceGoalRepository raceGoalRepository;
    private final AppUserRepository appUserRepository;
    private final RunnerProfileRepository runnerProfileRepository;

    public RaceGoalService(
        RaceGoalRepository raceGoalRepository,
        AppUserRepository appUserRepository,
        RunnerProfileRepository runnerProfileRepository
    ) {
        this.raceGoalRepository = raceGoalRepository;
        this.appUserRepository = appUserRepository;
        this.runnerProfileRepository = runnerProfileRepository;
    }

    @Transactional
    public CreateRaceGoalResponse createRaceGoal(UUID userId, CreateRaceGoalRequest request) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));

        if (runnerProfileRepository.findByUser_Id(userId).isEmpty()) {
            throw validation("profile", "missing", "Runner profile must be created before setting a race goal.");
        }

        if (raceGoalRepository.existsByUser_IdAndStatus(userId, RaceGoalStatus.ACTIVE)) {
            throw new ApiException("CONFLICT", "An active race goal already exists.", HttpStatus.CONFLICT,
                List.of(new ApiErrorDetail("raceGoal", "active_goal_exists")));
        }

        RaceDistanceType raceDistanceType = parseEnum(
            RaceDistanceType.class,
            request.raceDistanceType(),
            "raceDistanceType",
            "unsupported"
        );
        GoalStyle goalStyle = parseEnum(GoalStyle.class, request.goalStyle(), "goalStyle", "invalid");
        validateRaceDate(request.raceDate(), raceDistanceType);
        validateTargetTime(request.targetTimeSeconds());

        OffsetDateTime now = OffsetDateTime.now();
        RaceGoal goal = new RaceGoal();
        goal.setId(UUID.randomUUID());
        goal.setUser(user);
        goal.setRaceName(normalizeRaceName(request.raceName()));
        goal.setRaceDistanceType(raceDistanceType);
        goal.setRaceDate(request.raceDate());
        goal.setGoalStyle(goalStyle);
        goal.setTargetTimeSeconds(request.targetTimeSeconds());
        goal.setStatus(RaceGoalStatus.ACTIVE);
        goal.setCreatedAt(now);
        goal.setUpdatedAt(now);

        try {
            RaceGoal saved = raceGoalRepository.saveAndFlush(goal);
            return new CreateRaceGoalResponse(saved.getId(), saved.getStatus().name());
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException("CONFLICT", "An active race goal already exists.", HttpStatus.CONFLICT,
                List.of(new ApiErrorDetail("raceGoal", "active_goal_exists")));
        }
    }

    @Transactional(readOnly = true)
    public CurrentRaceGoalResponse getCurrentRaceGoal(UUID userId) {
        RaceGoal raceGoal = raceGoalRepository.findByUser_IdAndStatus(userId, RaceGoalStatus.ACTIVE)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "Active race goal not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("raceGoal", "not_found"))));

        return new CurrentRaceGoalResponse(
            raceGoal.getId(),
            raceGoal.getRaceDistanceType().name(),
            raceGoal.getRaceDate(),
            raceGoal.getGoalStyle().name(),
            raceGoal.getTargetTimeSeconds(),
            raceGoal.getStatus().name()
        );
    }

    private void validateRaceDate(LocalDate raceDate, RaceDistanceType raceDistanceType) {
        LocalDate todayUtc = LocalDate.now(ZoneId.of("UTC"));
        LocalDate minimumDate = switch (raceDistanceType) {
            case HALF_MARATHON -> todayUtc.plusWeeks(HALF_MIN_WEEKS);
            case MARATHON -> todayUtc.plusWeeks(MARATHON_MIN_WEEKS);
        };

        if (raceDate.isBefore(minimumDate)) {
            String message = raceDistanceType == RaceDistanceType.HALF_MARATHON
                ? "Race date must be at least 8 weeks away for half marathon goals."
                : "Race date must be at least 12 weeks away for marathon goals.";
            throw validation("raceDate", "too_soon", message);
        }
    }

    private void validateTargetTime(Integer targetTimeSeconds) {
        if (targetTimeSeconds != null && targetTimeSeconds <= 0) {
            throw validation("targetTimeSeconds", "invalid", "Target time must be positive.");
        }
    }

    private String normalizeRaceName(String raceName) {
        if (raceName == null) {
            return null;
        }
        String normalized = raceName.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String field, String issue) {
        if (value == null || value.isBlank()) {
            throw validation(field, issue, "Invalid value.");
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw validation(field, issue, "Invalid value.");
        }
    }

    private ApiException validation(String field, String issue, String message) {
        return new ApiException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST,
            List.of(new ApiErrorDetail(field, issue)));
    }
}

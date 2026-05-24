package com.company.runcoach.adaptation.service;

import com.company.runcoach.adaptation.api.CreateFatigueSignalRequest;
import com.company.runcoach.adaptation.api.CreateFatigueSignalResponse;
import com.company.runcoach.adaptation.domain.FatigueSignal;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.domain.ReadinessState;
import com.company.runcoach.adaptation.repo.FatigueSignalRepository;
import com.company.runcoach.adaptation.repo.InjuryFeedbackRepository;
import com.company.runcoach.common.api.ApiErrorDetail;
import com.company.runcoach.common.api.ApiException;
import com.company.runcoach.identity.domain.AppUser;
import com.company.runcoach.identity.repo.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FatigueSignalService {

    private static final String SOURCE_MOBILE_APP = "MOBILE_APP";

    private final FatigueSignalRepository fatigueSignalRepository;
    private final InjuryFeedbackRepository injuryFeedbackRepository;
    private final AppUserRepository appUserRepository;
    private final ReadinessService readinessService;

    public FatigueSignalService(
        FatigueSignalRepository fatigueSignalRepository,
        InjuryFeedbackRepository injuryFeedbackRepository,
        AppUserRepository appUserRepository,
        ReadinessService readinessService
    ) {
        this.fatigueSignalRepository = fatigueSignalRepository;
        this.injuryFeedbackRepository = injuryFeedbackRepository;
        this.appUserRepository = appUserRepository;
        this.readinessService = readinessService;
    }

    @Transactional
    public CreateFatigueSignalResponse createOrUpdate(UUID userId, CreateFatigueSignalRequest request) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));

        OffsetDateTime now = OffsetDateTime.now();
        FatigueSignal signal = fatigueSignalRepository.findByUser_IdAndSignalDateAndSource(userId, request.signalDate(), SOURCE_MOBILE_APP)
            .orElseGet(() -> {
                FatigueSignal created = new FatigueSignal();
                created.setId(UUID.randomUUID());
                created.setUser(user);
                created.setSignalDate(request.signalDate());
                created.setSource(SOURCE_MOBILE_APP);
                created.setCreatedAt(now);
                return created;
            });

        signal.setSleepScore(request.sleepScore());
        signal.setStressScore(request.stressScore());
        signal.setSorenessScore(request.sorenessScore());
        signal.setMotivationScore(request.motivationScore());
        signal.setIllnessFlag(Boolean.TRUE.equals(request.illnessFlag()));
        signal.setTooBusyFlag(Boolean.TRUE.equals(request.tooBusyFlag()));
        signal.setTravellingFlag(Boolean.TRUE.equals(request.travellingFlag()));
        signal.setNotes(normalize(request.notes()));
        signal.setUpdatedAt(now);
        FatigueSignal saved = fatigueSignalRepository.save(signal);

        ZoneId userZone = ZoneId.of(user.getTimezone());
        ZonedDateTime startOfSignalDate = request.signalDate().atStartOfDay(userZone);
        OffsetDateTime endOfSignalDateUtc = startOfSignalDate
            .plusDays(1)
            .minusSeconds(1)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toOffsetDateTime();

        InjuryFeedback latestInjury = injuryFeedbackRepository
            .findFirstByUser_IdAndReportedAtLessThanEqualOrderByReportedAtDescCreatedAtDesc(userId, endOfSignalDateUtc)
            .orElse(null);

        ReadinessState readinessState = readinessService.evaluate(saved, latestInjury);
        return new CreateFatigueSignalResponse(saved.getId(), readinessState);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

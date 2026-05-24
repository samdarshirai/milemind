package com.company.runcoach.adaptation.service;

import com.company.runcoach.adaptation.api.CreateInjuryFeedbackRequest;
import com.company.runcoach.adaptation.api.CreateInjuryFeedbackResponse;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.UUID;

@Service
public class InjuryFeedbackService {
    private static final Set<String> ALLOWED_BODY_REGIONS = Set.of(
        "LEFT_CALF", "RIGHT_CALF", "KNEE", "ANKLE", "HIP", "LOWER_BACK"
    );
    private static final Set<String> ALLOWED_PAIN_TYPES = Set.of(
        "SHARP", "DULL", "ACHING", "TIGHTNESS"
    );
    private static final Set<String> ALLOWED_ONSET_CONTEXTS = Set.of(
        "DURING_RUN", "AFTER_RUN", "ALL_DAY", "OTHER"
    );

    private final InjuryFeedbackRepository injuryFeedbackRepository;
    private final FatigueSignalRepository fatigueSignalRepository;
    private final AppUserRepository appUserRepository;
    private final ReadinessService readinessService;
    private final AdaptationDecisionService adaptationDecisionService;

    public InjuryFeedbackService(
        InjuryFeedbackRepository injuryFeedbackRepository,
        FatigueSignalRepository fatigueSignalRepository,
        AppUserRepository appUserRepository,
        ReadinessService readinessService,
        AdaptationDecisionService adaptationDecisionService
    ) {
        this.injuryFeedbackRepository = injuryFeedbackRepository;
        this.fatigueSignalRepository = fatigueSignalRepository;
        this.appUserRepository = appUserRepository;
        this.readinessService = readinessService;
        this.adaptationDecisionService = adaptationDecisionService;
    }

    @Transactional
    public CreateInjuryFeedbackResponse create(UUID userId, CreateInjuryFeedbackRequest request) {
        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND,
                List.of(new ApiErrorDetail("userId", "not_found"))));

        OffsetDateTime now = OffsetDateTime.now();
        NormalizedInjuryInput normalized = normalizeAndValidate(request);
        InjuryFeedback feedback = new InjuryFeedback();
        feedback.setId(UUID.randomUUID());
        feedback.setUser(user);
        feedback.setReportedAt(request.reportedAt());
        feedback.setHasPain(normalized.hasPain());
        feedback.setBodyRegion(normalized.bodyRegion());
        feedback.setPainType(normalized.painType());
        feedback.setSeverity(normalized.severity());
        feedback.setOnsetContext(normalized.onsetContext());
        feedback.setCanRun(normalized.canRun());
        feedback.setRedFlag(normalized.redFlag());
        feedback.setFreeText(normalize(request.freeText()));
        feedback.setCreatedAt(now);
        feedback.setUpdatedAt(now);

        InjuryFeedback saved = injuryFeedbackRepository.save(feedback);

        ZoneId userZone = ZoneId.of(user.getTimezone());
        LocalDate reportedDate = request.reportedAt().atZoneSameInstant(userZone).toLocalDate();
        FatigueSignal fatigueSignal = fatigueSignalRepository
            .findFirstByUser_IdAndSignalDateLessThanEqualOrderBySignalDateDescCreatedAtDesc(userId, reportedDate)
            .orElse(null);

        ReadinessState readinessState = readinessService.evaluate(fatigueSignal, saved);
        adaptationDecisionService.adaptFromInjuryFeedback(userId, saved, readinessState);
        return new CreateInjuryFeedbackResponse(saved.getId(), readinessState);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private NormalizedInjuryInput normalizeAndValidate(CreateInjuryFeedbackRequest request) {
        Boolean hasPain = request.hasPain();
        Boolean canRun = request.canRun();
        Boolean redFlag = request.redFlag();
        String bodyRegion = normalize(request.bodyRegion());
        String painType = normalize(request.painType());
        String onsetContext = normalize(request.onsetContext());
        Integer severity = request.severity();
        boolean anyPainFieldProvided = bodyRegion != null || painType != null || onsetContext != null || severity != null;
        boolean anyRiskFieldProvided = canRun != null || redFlag != null;
        boolean riskWithoutPainFields = anyRiskFieldProvided && !anyPainFieldProvided;
        boolean anyPainOrRiskFieldProvided = anyPainFieldProvided || riskWithoutPainFields;
        boolean explicitNoPain = Boolean.FALSE.equals(hasPain);
        boolean explicitPain = Boolean.TRUE.equals(hasPain);

        if (explicitNoPain) {
            if (anyPainOrRiskFieldProvided) {
                throw new ApiException(
                    "VALIDATION_ERROR",
                    "Validation failed.",
                    HttpStatus.BAD_REQUEST,
                    List.of(new ApiErrorDetail("hasPain", "conflicts_with_pain_fields"))
                );
            }
            return new NormalizedInjuryInput(false, null, null, null, null, true, false);
        }

        if (!explicitPain && !anyPainOrRiskFieldProvided) {
            return new NormalizedInjuryInput(false, null, null, null, null, true, false);
        }

        if (!explicitPain && riskWithoutPainFields) {
            throw new ApiException(
                "VALIDATION_ERROR",
                "Validation failed.",
                HttpStatus.BAD_REQUEST,
                List.of(new ApiErrorDetail("hasPain", "required_when_risk_fields_present"))
            );
        }

        List<ApiErrorDetail> details = new ArrayList<>();
        if (bodyRegion == null) {
            details.add(new ApiErrorDetail("bodyRegion", "required_when_pain_reported"));
        }
        if (painType == null) {
            details.add(new ApiErrorDetail("painType", "required_when_pain_reported"));
        }
        if (onsetContext == null) {
            details.add(new ApiErrorDetail("onsetContext", "required_when_pain_reported"));
        }
        if (canRun == null) {
            details.add(new ApiErrorDetail("canRun", "required_when_pain_reported"));
        }
        if (severity == null) {
            details.add(new ApiErrorDetail("severity", "required_when_pain_reported"));
        } else if (severity < 0 || severity > 10) {
            details.add(new ApiErrorDetail("severity", "out_of_range"));
        }
        if (bodyRegion != null && !ALLOWED_BODY_REGIONS.contains(bodyRegion)) {
            details.add(new ApiErrorDetail("bodyRegion", "invalid_value"));
        }
        if (painType != null && !ALLOWED_PAIN_TYPES.contains(painType)) {
            details.add(new ApiErrorDetail("painType", "invalid_value"));
        }
        if (onsetContext != null && !ALLOWED_ONSET_CONTEXTS.contains(onsetContext)) {
            details.add(new ApiErrorDetail("onsetContext", "invalid_value"));
        }

        if (!details.isEmpty()) {
            throw new ApiException("VALIDATION_ERROR", "Validation failed.", HttpStatus.BAD_REQUEST, details);
        }

        return new NormalizedInjuryInput(
            true,
            bodyRegion,
            painType,
            severity,
            onsetContext,
            canRun,
            Boolean.TRUE.equals(request.redFlag())
        );
    }

    private record NormalizedInjuryInput(
        boolean hasPain,
        String bodyRegion,
        String painType,
        Integer severity,
        String onsetContext,
        Boolean canRun,
        boolean redFlag
    ) {
    }
}

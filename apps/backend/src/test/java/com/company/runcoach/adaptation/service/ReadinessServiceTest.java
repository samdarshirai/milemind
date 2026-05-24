package com.company.runcoach.adaptation.service;

import com.company.runcoach.adaptation.domain.FatigueSignal;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.domain.ReadinessState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadinessServiceTest {

    private final ReadinessService readinessService = new ReadinessService();

    @Test
    void mapsReadyWhenNoConcerningSignals() {
        FatigueSignal fatigue = fatigue(4, 2, 2, 4, false, false, false);
        InjuryFeedback injury = injury(1, false, true);

        assertEquals(ReadinessState.READY, readinessService.evaluate(fatigue, injury));
    }

    @Test
    void mapsCautionForModerateFatigue() {
        FatigueSignal fatigue = fatigue(3, 3, 2, 3, false, false, false);
        assertEquals(ReadinessState.CAUTION, readinessService.evaluate(fatigue, null));
    }

    @Test
    void mapsHighRiskForHighPainSeverity() {
        InjuryFeedback injury = injury(8, false, false);
        assertEquals(ReadinessState.HIGH_RISK, readinessService.evaluate(null, injury));
    }

    @Test
    void painSeverityThresholdHandling() {
        assertEquals(ReadinessState.CAUTION, readinessService.evaluate(null, injury(4, false, true)));
        assertEquals(ReadinessState.HIGH_RISK, readinessService.evaluate(null, injury(7, false, true)));
    }

    @Test
    void mapsHighRiskForRedFlagPain() {
        assertEquals(ReadinessState.HIGH_RISK, readinessService.evaluate(null, injury(2, true, true)));
    }

    @Test
    void mapsHighRiskWhenRunnerCannotRun() {
        assertEquals(ReadinessState.HIGH_RISK, readinessService.evaluate(null, injury(2, false, false)));
    }

    @Test
    void mapsHighRiskForSharpPainDuringRunEvenAtModerateSeverity() {
        InjuryFeedback injuryFeedback = injury(5, false, true);
        injuryFeedback.setPainType("SHARP");
        injuryFeedback.setOnsetContext("DURING_RUN");
        assertEquals(ReadinessState.HIGH_RISK, readinessService.evaluate(null, injuryFeedback));
    }

    private FatigueSignal fatigue(int sleep, int stress, int soreness, int motivation, boolean illness, boolean busy, boolean travel) {
        FatigueSignal fatigueSignal = new FatigueSignal();
        fatigueSignal.setSleepScore(sleep);
        fatigueSignal.setStressScore(stress);
        fatigueSignal.setSorenessScore(soreness);
        fatigueSignal.setMotivationScore(motivation);
        fatigueSignal.setIllnessFlag(illness);
        fatigueSignal.setTooBusyFlag(busy);
        fatigueSignal.setTravellingFlag(travel);
        return fatigueSignal;
    }

    private InjuryFeedback injury(int severity, boolean redFlag, boolean canRun) {
        InjuryFeedback injuryFeedback = new InjuryFeedback();
        injuryFeedback.setSeverity(severity);
        injuryFeedback.setRedFlag(redFlag);
        injuryFeedback.setCanRun(canRun);
        return injuryFeedback;
    }
}

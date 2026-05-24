package com.company.runcoach.adaptation.service;

import com.company.runcoach.adaptation.domain.FatigueSignal;
import com.company.runcoach.adaptation.domain.InjuryFeedback;
import com.company.runcoach.adaptation.domain.ReadinessState;
import org.springframework.stereotype.Service;

@Service
public class ReadinessService {

    public ReadinessState evaluate(FatigueSignal fatigueSignal, InjuryFeedback injuryFeedback) {
        if (injuryFeedback != null) {
            Integer severity = injuryFeedback.getSeverity();
            boolean sharpDuringRun = "SHARP".equals(injuryFeedback.getPainType())
                && "DURING_RUN".equals(injuryFeedback.getOnsetContext());
            if ((severity != null && severity >= 7) || injuryFeedback.isRedFlag() || Boolean.FALSE.equals(injuryFeedback.getCanRun())
                || sharpDuringRun) {
                return ReadinessState.HIGH_RISK;
            }
            if (severity != null && severity >= 4) {
                return ReadinessState.CAUTION;
            }
        }

        if (fatigueSignal != null) {
            int highStressOrSoreness = 0;
            if (fatigueSignal.getStressScore() >= 4) {
                highStressOrSoreness++;
            }
            if (fatigueSignal.getSorenessScore() >= 4) {
                highStressOrSoreness++;
            }

            if (fatigueSignal.isIllnessFlag() || (fatigueSignal.getSleepScore() <= 2 && fatigueSignal.getMotivationScore() <= 2)
                || (highStressOrSoreness == 2 && fatigueSignal.getSleepScore() <= 2)) {
                return ReadinessState.HIGH_RISK;
            }

            if (fatigueSignal.getSleepScore() <= 3 || fatigueSignal.getMotivationScore() <= 3
                || fatigueSignal.getStressScore() >= 3 || fatigueSignal.getSorenessScore() >= 3
                || fatigueSignal.isTooBusyFlag() || fatigueSignal.isTravellingFlag()) {
                return ReadinessState.CAUTION;
            }
        }

        return ReadinessState.READY;
    }
}

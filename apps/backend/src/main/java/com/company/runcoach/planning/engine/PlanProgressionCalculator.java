package com.company.runcoach.planning.engine;

import com.company.runcoach.profile.domain.ExperienceLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PlanProgressionCalculator {

    public BigDecimal nextWeekVolume(BigDecimal previous, ExperienceLevel level) {
        BigDecimal increase = switch (level) {
            case BEGINNER -> new BigDecimal("1.06");
            case INTERMEDIATE -> new BigDecimal("1.09");
            case ADVANCED -> new BigDecimal("1.10");
        };
        return previous.multiply(increase).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal recoveryWeekVolume(BigDecimal previous) {
        return previous.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal taperWeekMinusTwo(BigDecimal previous) {
        return previous.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal raceWeekVolume(BigDecimal previous) {
        return previous.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP);
    }
}

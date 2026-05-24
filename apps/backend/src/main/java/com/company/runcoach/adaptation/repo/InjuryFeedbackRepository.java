package com.company.runcoach.adaptation.repo;

import com.company.runcoach.adaptation.domain.InjuryFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface InjuryFeedbackRepository extends JpaRepository<InjuryFeedback, UUID> {
    Optional<InjuryFeedback> findFirstByUser_IdAndReportedAtBetweenOrderByReportedAtDescCreatedAtDesc(
        UUID userId,
        OffsetDateTime start,
        OffsetDateTime end
    );
    Optional<InjuryFeedback> findFirstByUser_IdAndReportedAtLessThanEqualOrderByReportedAtDescCreatedAtDesc(UUID userId, OffsetDateTime reportedAt);
    boolean existsByUser_IdAndReportedAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);
}

package com.company.runcoach.adaptation.repo;

import com.company.runcoach.adaptation.domain.AdaptationDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdaptationDecisionRepository extends JpaRepository<AdaptationDecision, UUID> {
    Optional<AdaptationDecision> findFirstByTrainingPlan_IdOrderByCreatedAtDesc(UUID trainingPlanId);
}

package com.company.runcoach.adaptation.repo;

import com.company.runcoach.adaptation.domain.AdaptationDecision;
import com.company.runcoach.adaptation.domain.AdaptationTriggerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdaptationDecisionRepository extends JpaRepository<AdaptationDecision, UUID> {
    Optional<AdaptationDecision> findFirstByTrainingPlan_IdOrderByCreatedAtDesc(UUID trainingPlanId);
    List<AdaptationDecision> findByTrainingPlan_IdAndTriggerTypeOrderByCreatedAtAsc(UUID trainingPlanId, AdaptationTriggerType triggerType);
}

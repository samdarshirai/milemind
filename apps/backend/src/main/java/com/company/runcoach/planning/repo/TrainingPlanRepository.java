package com.company.runcoach.planning.repo;

import com.company.runcoach.planning.domain.PlanStatus;
import com.company.runcoach.planning.domain.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, UUID> {
    Optional<TrainingPlan> findByIdAndUser_Id(UUID id, UUID userId);
    Optional<TrainingPlan> findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(UUID userId, Collection<PlanStatus> statuses);
    Optional<TrainingPlan> findFirstByUser_IdAndRaceGoal_IdAndStatusInOrderByCreatedAtDesc(
        UUID userId,
        UUID raceGoalId,
        Collection<PlanStatus> statuses
    );
    List<TrainingPlan> findByUser_IdAndRaceGoal_IdAndStatusIn(UUID userId, UUID raceGoalId, Collection<PlanStatus> statuses);
}

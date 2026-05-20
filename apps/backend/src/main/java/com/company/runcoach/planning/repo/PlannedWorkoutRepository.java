package com.company.runcoach.planning.repo;

import com.company.runcoach.planning.domain.PlannedWorkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlannedWorkoutRepository extends JpaRepository<PlannedWorkout, UUID> {
    List<PlannedWorkout> findByTrainingPlan_IdOrderByScheduledDateAsc(UUID trainingPlanId);
    java.util.Optional<PlannedWorkout> findByIdAndUser_Id(UUID id, UUID userId);
}

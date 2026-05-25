package com.company.runcoach.planning.repo;

import com.company.runcoach.planning.domain.WorkoutCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkoutCompletionRepository extends JpaRepository<WorkoutCompletion, UUID> {
    Optional<WorkoutCompletion> findByPlannedWorkout_Id(UUID plannedWorkoutId);
    java.util.List<WorkoutCompletion> findByPlannedWorkout_TrainingPlan_Id(UUID trainingPlanId);
}
